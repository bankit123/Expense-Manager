package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.*;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransactionSchedule;

public class RecurringSchedulerWorker extends Worker {

    private final AppDatabase db;

    public RecurringSchedulerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Date today = normalizeDate(new Date());
            List<RecurringTransaction> activeList = db.recurringTransactionDao().getAllActiveSync();

            for (RecurringTransaction rt : activeList) {
                if (rt == null || "paused".equalsIgnoreCase(rt.status)) continue;
                createOrUpdateSchedules(rt, today);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void createOrUpdateSchedules(RecurringTransaction rt, Date today) {
        List<RecurringTransactionSchedule> existing =
                db.recurringScheduleDao().getAllByRecurringIdSync(rt.recurring_id);
        if (existing == null) existing = new ArrayList<>();

        // 🔁 1️⃣ Mark old upcoming as missed if below today
        for (RecurringTransactionSchedule s : existing) {
            if ("upcoming".equalsIgnoreCase(s.status) &&
                    s.scheduled_date != null &&
                    s.scheduled_date.before(today)) {
                s.status = "missed";
                s.updated_at = new Date();
                db.recurringScheduleDao().update(s);
            }
        }

        // ✅ 2️⃣ If there’s already one upcoming, skip creating a new one
        boolean hasUpcoming = false;
        for (RecurringTransactionSchedule s : existing) {
            if ("upcoming".equalsIgnoreCase(s.status)) {
                hasUpcoming = true;
                break;
            }
        }
        if (hasUpcoming) return; // stop — one upcoming already exists

        // 3️⃣ Find latest scheduled date
        Date lastDate = rt.start_date != null ? normalizeDate(rt.start_date) : today;
        for (RecurringTransactionSchedule s : existing) {
            if (s.scheduled_date != null && s.scheduled_date.after(lastDate)) {
                lastDate = normalizeDate(s.scheduled_date);
            }
        }

        // ✅ Pass start_date when calculating next date
        Date nextDate = getNextDate(lastDate, rt.frequency, rt.start_date);

        // 4️⃣ Fill missed schedules until today
        while (nextDate.before(today)) {
            if (!db.recurringScheduleDao().existsSchedule(rt.recurring_id, nextDate)) {
                RecurringTransactionSchedule missed = new RecurringTransactionSchedule(
                        rt.recurring_id,
                        nextDate,
                        rt.amount,
                        "missed"
                );
                missed.created_at = new Date();
                missed.updated_at = new Date();
                db.recurringScheduleDao().insert(missed);
            }
            // ✅ Also pass start_date here
            nextDate = getNextDate(nextDate, rt.frequency, rt.start_date);
        }

        // 5️⃣ Create upcoming schedule only if none exists already
        if (!db.recurringScheduleDao().existsSchedule(rt.recurring_id, nextDate)) {
            RecurringTransactionSchedule upcoming = new RecurringTransactionSchedule(
                    rt.recurring_id,
                    nextDate,
                    rt.amount,
                    "upcoming"
            );
            upcoming.created_at = new Date();
            upcoming.updated_at = new Date();
            db.recurringScheduleDao().insert(upcoming);
        }

        // 6️⃣ Update recurring transaction’s next due date
        rt.next_due_date = nextDate;
        rt.updated_at = new Date();
        db.recurringTransactionDao().updateRecurring(rt);
    }

    private Date getNextDate(Date base, String freq, Date startDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);

        switch (freq.toLowerCase(Locale.ROOT)) {
            case "daily":
                c.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case "weekly":
                c.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            case "monthly":
                int baseDay = 1;
                if (startDate != null) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startDate);
                    baseDay = startCal.get(Calendar.DAY_OF_MONTH);
                }

                // Move to next month
                c.add(Calendar.MONTH, 1);

                // Adjust if that month doesn’t have the original day
                int maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (baseDay > maxDay) {
                    c.set(Calendar.DAY_OF_MONTH, maxDay);
                } else {
                    c.set(Calendar.DAY_OF_MONTH, baseDay);
                }
                break;

            case "yearly":
                c.add(Calendar.YEAR, 1);
                break;
        }

        return normalizeDate(c.getTime());
    }

    private Date normalizeDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
