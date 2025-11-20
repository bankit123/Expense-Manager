package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;

public class RecurringTransactionWorker extends Worker {

    private final AppDatabase db;

    public RecurringTransactionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        long recurringId = getInputData().getLong("recurring_id", -1);
        if (recurringId == -1) return Result.failure();

        try {
            RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(recurringId);
            if (rt == null || !"active".equalsIgnoreCase(rt.status)) return Result.success();

            // Create a Transaction
            Transaction t = new Transaction();
            t.user_id = rt.user_id;
            t.subtype_id = rt.subtype_id;
            t.category_id = rt.category_id;
            t.amount = rt.amount;
            t.type = rt.type;
            t.notes = rt.title + (rt.notes != null ? " - " + rt.notes : "");
            t.date = new Date();
            t.created_at = new Date();
            t.updated_at = new Date();

            db.transactionDao().insert(t);

            // Update next due date & status
            Calendar cal = Calendar.getInstance();
            cal.setTime(rt.next_due_date != null ? rt.next_due_date : new Date());

            switch (rt.frequency) {
                case "daily": cal.add(Calendar.DAY_OF_MONTH, 1); break;
                case "weekly": cal.add(Calendar.WEEK_OF_YEAR, 1); break;
                case "monthly": cal.add(Calendar.MONTH, 1); break;
                case "yearly": cal.add(Calendar.YEAR, 1); break;
            }

            rt.completed_payments++;

            // Mark completed if not repeat forever
            if (!rt.repeat_forever && rt.completed_payments >= rt.total_payments) {
                rt.status = "completed";
            } else {
                rt.next_due_date = cal.getTime();
            }

            rt.updated_at = new Date();
            db.recurringTransactionDao().update(rt);

            // Schedule next transaction if still active
            if ("active".equalsIgnoreCase(rt.status)) {
                scheduleNext(getApplicationContext(), rt.recurring_id, rt.next_due_date);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RecurringWorker", "Error: " + e.getMessage());
            return Result.failure();
        }
    }

    public static void scheduleNext(Context context, long recurringId, Date nextDue) {
        if (nextDue == null) return;

        long delay = nextDue.getTime() - System.currentTimeMillis();
        if (delay < 0) delay = 0;

        Data input = new Data.Builder().putLong("recurring_id", recurringId).build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RecurringTransactionWorker.class)
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(input)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
