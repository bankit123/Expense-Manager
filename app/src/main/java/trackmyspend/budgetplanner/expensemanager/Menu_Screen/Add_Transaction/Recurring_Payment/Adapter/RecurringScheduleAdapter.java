package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Adapter;

import static trackmyspend.budgetplanner.expensemanager.Util.DateUtils.normalizeDate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.*;
import trackmyspend.budgetplanner.expensemanager.R;

public class RecurringScheduleAdapter extends RecyclerView.Adapter<RecurringScheduleAdapter.ScheduleViewHolder> {

    private final List<RecurringTransactionSchedule> list = new ArrayList<>();
    private final Context context;
    private final AppDatabase db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public RecurringScheduleAdapter(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
    }

    public void setData(List<RecurringTransactionSchedule> newList) {
        list.clear();
        newList.sort((a, b) -> b.scheduled_date.compareTo(a.scheduled_date));
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_schedule, parent, false);
        return new ScheduleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        RecurringTransactionSchedule item = list.get(position);
        holder.tvDate.setText(sdf.format(item.scheduled_date));
        holder.tvAmount.setText(String.format("₹%.2f", item.amount));

        Date today = normalizeDate(new Date());
        Date scheduled = normalizeDate(item.scheduled_date);

        // Status color & button logic
        switch (item.status.toLowerCase(Locale.ROOT)) {
            case "completed":
                holder.tvStatus.setText("Completed");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.btnPay.setVisibility(View.GONE);
                holder.btnSkip.setVisibility(View.GONE);
                break;
            case "missed":
                holder.tvStatus.setText("Missed");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
                holder.btnPay.setVisibility(View.VISIBLE);
                holder.btnSkip.setVisibility(View.GONE);
                break;
            case "skipped":
                holder.tvStatus.setText("Skipped");
                holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                holder.btnPay.setVisibility(View.GONE);
                holder.btnSkip.setVisibility(View.GONE);
                break;
            default:
                if (scheduled.equals(today)) {
                    holder.tvStatus.setText("Pay Today 💸");
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.nav_icon_active));
                } else {
                    holder.tvStatus.setText("Upcoming");
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.upcoming_color));
                }
                holder.btnPay.setVisibility(View.VISIBLE);
                holder.btnSkip.setVisibility(View.VISIBLE);
        }

        holder.btnPay.setOnClickListener(v -> confirmPay(item));
        holder.btnSkip.setOnClickListener(v -> confirmSkip(item));
    }

    private void confirmPay(RecurringTransactionSchedule s) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Payment")
                .setMessage("Mark this payment as completed?")
                .setPositiveButton("Yes", (d, w) -> markAsPaid(s))
                .setNegativeButton("No", null).show();
    }

    private void confirmSkip(RecurringTransactionSchedule s) {
        new AlertDialog.Builder(context)
                .setTitle("Skip Payment")
                .setMessage("Are you sure you want to skip this schedule?")
                .setPositiveButton("Yes", (d, w) -> skipSchedule(s))
                .setNegativeButton("No", null).show();
    }

    private void markAsPaid(RecurringTransactionSchedule s) {
        new Thread(() -> {
            try {
                s.status = "completed";
                s.updated_at = new Date();
                db.recurringScheduleDao().update(s);

                RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(s.recurring_id);
                if (rt == null) return;

                // increment progress
                rt.completed_payments += 1;
                rt.updated_at = new Date();

                Date nextDate = getNextDate(s.scheduled_date, rt.frequency);
                if (!db.recurringScheduleDao().existsSchedule(rt.recurring_id, nextDate)) {
                    RecurringTransactionSchedule newSchedule = new RecurringTransactionSchedule(
                            rt.recurring_id, nextDate, rt.amount, "upcoming");
                    newSchedule.created_at = new Date();
                    newSchedule.updated_at = new Date();
                    db.recurringScheduleDao().insert(newSchedule);
                }
                rt.next_due_date = nextDate;
                db.recurringTransactionDao().update(rt);

                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Payment completed ✅", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void skipSchedule(RecurringTransactionSchedule s) {
        new Thread(() -> {
            try {
                s.status = "skipped";
                s.updated_at = new Date();
                db.recurringScheduleDao().update(s);

                RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(s.recurring_id);
                if (rt == null) return;

                Date nextDate = getNextDate(s.scheduled_date, rt.frequency);
                if (!db.recurringScheduleDao().existsSchedule(rt.recurring_id, nextDate)) {
                    RecurringTransactionSchedule newSchedule = new RecurringTransactionSchedule(
                            rt.recurring_id, nextDate, rt.amount, "upcoming");
                    newSchedule.created_at = new Date();
                    newSchedule.updated_at = new Date();
                    db.recurringScheduleDao().insert(newSchedule);
                }

                rt.next_due_date = nextDate;
                rt.updated_at = new Date();
                db.recurringTransactionDao().update(rt);

                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Payment skipped ✅", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Date getNextDate(Date base, String freq) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        switch (freq.toLowerCase(Locale.ROOT)) {
            case "daily": c.add(Calendar.DAY_OF_MONTH, 1); break;
            case "weekly": c.add(Calendar.WEEK_OF_YEAR, 1); break;
            case "monthly": c.add(Calendar.MONTH, 1); break;
            case "yearly": c.add(Calendar.YEAR, 1); break;
        }
        return normalizeDate(c.getTime());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAmount, tvStatus, btnPay, btnSkip;
        ScheduleViewHolder(@NonNull View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvScheduleDate);
            tvAmount = v.findViewById(R.id.tvScheduleAmount);
            tvStatus = v.findViewById(R.id.tvScheduleStatus);
            btnPay = v.findViewById(R.id.btnPaySchedule);
            btnSkip = v.findViewById(R.id.btnSkipSchedule);
        }
    }
}
