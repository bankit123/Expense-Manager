package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.DateHeader;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class AccountTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private final Context context;
    private final List<Object> items = new ArrayList<>();
    private final AppDatabase db;
    private final SimpleDateFormat sdfTime =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public AccountTransactionsAdapter(Context context, List<Transaction> transactions, AppDatabase db) {
        this.context = context;
        this.db = db;
        groupTransactionsByDate(transactions);
    }

    private void groupTransactionsByDate(List<Transaction> transactions) {
        items.clear();
        Map<String, DateHeader> headers = new LinkedHashMap<>();
        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        for (Transaction t : transactions) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.date);

            String key;
            if (isSameDay(cal, today)) {
                key = "Today";
            } else if (isSameDay(cal, yesterday)) {
                key = "Yesterday";
            } else {
                key = sdfDate.format(t.date);
            }

            // Create DateHeader if not exist
            if (!headers.containsKey(key)) {
                headers.put(key, new DateHeader(cal.getTime(),0,0));
                grouped.put(key, new ArrayList<>());
            }

            // Update totals
            DateHeader header = headers.get(key);
            if ("Income".equalsIgnoreCase(t.type)) {
                header.incomeTotal += t.amount;
            } else {
                header.expenseTotal += t.amount;
            }

            grouped.get(key).add(t);
        }

        // Merge into items list
        for (String key : grouped.keySet()) {
            items.add(headers.get(key)); // header
            items.addAll(grouped.get(key)); // transactions
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof DateHeader) ? TYPE_HEADER : TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.mockup_item_transaction_grouped_date, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.mockup_item_transaction_entry, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            DateHeader header = (DateHeader) item;
            HeaderViewHolder h = (HeaderViewHolder) holder;

            // Format date parts
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM.yyyy", Locale.getDefault());

            h.tvDay.setText(dayFormat.format(header.date));
            h.tvWeekday.setText(weekdayFormat.format(header.date));
            h.tvMonthYear.setText(monthYearFormat.format(header.date));

            h.tvIncomeTotal.setText("+" + CurrencyFormatterUtil.format(header.incomeTotal));
            h.tvIncomeTotal.setTextColor(Color.parseColor("#388E3C"));

            h.tvExpenseTotal.setVisibility(View.GONE);
//            h.tvExpenseTotal.setTextColor(Color.parseColor("#DA6C6C"));

        } else if (holder instanceof TransactionViewHolder) {
            Transaction t = (Transaction) item;
            TransactionViewHolder vh = (TransactionViewHolder) holder;

            vh.tvAmount.setText(CurrencyFormatterUtil.format(t.amount));
            vh.tvDate.setText(sdfTime.format(t.date));
            vh.tvSourceName.setText(t.source_name != null ? t.source_name : "—");

            if (t.source_name != null && !t.source_name.trim().isEmpty()) {
                vh.tvSourceName.setVisibility(View.VISIBLE);
                vh.tvSourceName.setText(t.source_name);

            } else {
                vh.tvSourceName.setVisibility(View.GONE);

            }

            if (t.notes != null && !t.notes.trim().isEmpty()) {
                vh.tvNotes.setVisibility(View.VISIBLE);
                vh.tvNotes.setText(t.notes);
                vh.hr.setVisibility(View.VISIBLE);

            } else {
                vh.tvNotes.setVisibility(View.GONE);
                vh.hr.setVisibility(View.GONE);

            }

            // Category
            if (t.category_id != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Category category = db.categoryDao().getCategoryById(t.category_id);
                    if (category != null) {
                        int resId = context.getResources().getIdentifier(category.icon, "drawable", context.getPackageName());
                        ((Activity) context).runOnUiThread(() -> {
                            vh.tvCategoryName.setText(category.name);
                            vh.ivCategoryIcon.setImageResource(resId);

                            int baseColor = Color.parseColor(category.colorHex);
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(baseColor);
                            vh.bgIconCategory.setBackground(bg);

                            int darkerColor = manipulateColor(baseColor, 0.5f);
                            vh.ivCategoryIcon.setColorFilter(darkerColor);
                        });
                    }
                });
            }

            // Subtype
            if (t.subtype_id != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Subtype subtype = db.subtypeDao().getSubtypeById(t.subtype_id);
                    if (subtype != null) {
                        int resId = context.getResources().getIdentifier(subtype.icon, "drawable", context.getPackageName());
                        ((Activity) context).runOnUiThread(() -> {
                            vh.tvSubtypeName.setText(subtype.name);
                            vh.ivSubtypeIcon.setImageResource(resId);

                            int baseColor = Color.parseColor(subtype.backgroundColorHex);
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(baseColor);
                            vh.bgIconSubType.setBackground(bg);

                            int darkerColor = manipulateColor(baseColor, 0.6f);
                            vh.ivSubtypeIcon.setColorFilter(darkerColor);
                        });
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // HEADER HOLDER
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvWeekday, tvMonthYear, tvIncomeTotal, tvExpenseTotal;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvWeekday = itemView.findViewById(R.id.tvWeekday);
            tvMonthYear = itemView.findViewById(R.id.tvMonthYear);
            tvIncomeTotal = itemView.findViewById(R.id.tvIncomeTotal);
            tvExpenseTotal = itemView.findViewById(R.id.tvExpenseTotal);
        }
    }

    // TRANSACTION HOLDER
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSubtypeName, tvSourceName, tvNotes, tvAmount, tvDate;
        ImageView ivCategoryIcon, ivSubtypeIcon;
        FrameLayout bgIconCategory, bgIconSubType;
        View hr;


        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            hr = itemView.findViewById(R.id.hr);
            bgIconCategory = itemView.findViewById(R.id.bgIconCategory);
            bgIconSubType = itemView.findViewById(R.id.bgIconSubType);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
            tvSourceName = itemView.findViewById(R.id.tvSourceName);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    private int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }
}
