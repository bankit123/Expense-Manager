package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Recurring_payment_Details;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

public class RecurringPaymentAdapter extends RecyclerView.Adapter<RecurringPaymentAdapter.ViewHolder> {

    private final Context context;
    private final List<RecurringTransaction> recurringList;
    private final AppDatabase db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public RecurringPaymentAdapter(Context context, List<RecurringTransaction> recurringList) {
        this.context = context;
        this.recurringList = recurringList;
        this.db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_recurring_payment, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringTransaction item = recurringList.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvFrequency.setText(capitalize(item.frequency));
        holder.tvNextDue.setText(item.next_due_date != null
                ? "Next: " + sdf.format(item.next_due_date)
                : "Next: N/A");
        holder.tvAmount.setText(CurrencyFormatterUtil.format(item.amount));

        // 🎨 STATUS + PROGRESS (neutral badge) + ICON COLOR (state color)
        String displayText;
        int iconColor;

        switch (item.status.toLowerCase(Locale.ROOT)) {
            case "paused":
                // 🔴 Paused: show progress text (3/12) or Repeat Forever, color = red
                if (item.repeat_forever) {
                    displayText = "Repeat Forever";
                } else {
                    displayText = item.total_payments > 0
                            ? item.completed_payments + "/" + item.total_payments
                            : "0/0";
                }
                iconColor = ContextCompat.getColor(context, R.color.red_recurring_payment);
                holder.tvCount.setTextColor(iconColor);
                break;

            case "completed":
                // 🟢 Completed: show full progress (e.g., 12/12), color = green
                if (item.repeat_forever) {
                    displayText = "Repeat Forever";
                } else {
                    displayText = item.total_payments > 0
                            ? item.total_payments + "/" + item.total_payments
                            : "0/0";
                }
                iconColor = ContextCompat.getColor(context, R.color.green_500);
                holder.tvCount.setTextColor(iconColor);
                break;

            default: // 🟦 Active
                if (item.repeat_forever) {
                    displayText = "Repeat Forever";
                } else {
                    displayText = item.total_payments > 0
                            ? item.completed_payments + "/" + item.total_payments
                            : "0/0";
                }
                iconColor = ContextCompat.getColor(context, R.color.nav_icon_active);
                holder.tvCount.setTextColor(iconColor);
                break;
        }
        holder.tvCount.setText(displayText);

        // 🎯 Apply color only to circular icon background
        holder.bgIconRecurring.setBackgroundTintList(ColorStateList.valueOf(iconColor));

        // 📝 Notes
        holder.tvNotes.setText(item.notes != null && !item.notes.isEmpty()
                ? item.notes
                : "No notes added");

        // 🏷️ Load Category + Subtype icons
        Executors.newSingleThreadExecutor().execute(() -> {
            Category category = item.category_id != 0 ? db.categoryDao().getCategoryById(item.category_id) : null;
            Subtype subtype = item.subtype_id != 0 ? db.subtypeDao().getSubtypeById(item.subtype_id) : null;

            ((Activity) context).runOnUiThread(() -> {
                if (category != null) {
                    holder.tvCategory.setText(category.name);
                    int iconRes = context.getResources().getIdentifier(category.icon, "drawable", context.getPackageName());
                    holder.ivCategory.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_category);
                }

                if (subtype != null) {
                    holder.tvSubtype.setText(subtype.name);
                    int iconRes = context.getResources().getIdentifier(subtype.icon, "drawable", context.getPackageName());
                    holder.ivSubtype.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_wallet);
                }
            });
        });

        // 👉 Click -> open details
        holder.contentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, Recurring_payment_Details.class);
            intent.putExtra("recurring_id", item.recurring_id);
            context.startActivity(intent);
        });

        // 🧹 Swipe Delete
        SwipeRevealHelper.attach(
                holder.contentLayout,
                holder.deleteBackground,
                () -> Executors.newSingleThreadExecutor().execute(() -> {
                    RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(item.recurring_id);
                    if (rt != null) db.recurringTransactionDao().delete(rt);

                    ((Activity) context).runOnUiThread(() -> {
                        int pos = holder.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < recurringList.size()) {
                            recurringList.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, recurringList.size());
                        }
                    });
                })
        );
    }

    @Override
    public int getItemCount() {
        return recurringList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvFrequency, tvNextDue, tvAmount, tvCount, tvNotes, tvCategory, tvSubtype;
        ImageView ivCategory, ivSubtype;
        LinearLayout contentLayout, deleteBackground;
        FrameLayout bgIconRecurring;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bgIconRecurring = itemView.findViewById(R.id.bgIconRecurring);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            deleteBackground = itemView.findViewById(R.id.btnDeleteBackground);
            tvTitle = itemView.findViewById(R.id.tvRecurringTitle);
            tvFrequency = itemView.findViewById(R.id.tvRecurringFrequency);
            tvNextDue = itemView.findViewById(R.id.tvRecurringNextDue);
            tvAmount = itemView.findViewById(R.id.tvRecurringAmount);
            tvCount = itemView.findViewById(R.id.tvRecurringCount);
            tvNotes = itemView.findViewById(R.id.tvRecurringNotes);
            tvCategory = itemView.findViewById(R.id.tvRecurringCategory);
            tvSubtype = itemView.findViewById(R.id.tvRecurringSubtype);
            ivCategory = itemView.findViewById(R.id.ivRecurringCategoryIcon);
            ivSubtype = itemView.findViewById(R.id.ivRecurringSubtypeIcon);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
