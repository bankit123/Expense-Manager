package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.AddTransactionActivity;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TransactionGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private final Context context;
    private final List<Object> items;
    private final AppDatabase db;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public TransactionGroupedAdapter(Context context, List<Object> items, AppDatabase db) {
        this.context = context;
        this.items = items;
        this.db = db;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = items.get(position);
        if (obj instanceof DateHeader) return TYPE_HEADER;
        if (obj instanceof Transaction) return TYPE_TRANSACTION;
        if ("AD_PLACEHOLDER".equals(obj)) return 99; // custom ad type
        return TYPE_TRANSACTION;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == 99) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.banner_container_dynamic, parent, false);
            return new AdHolder(view);
        }

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.mockup_item_transaction_grouped_date, parent, false);
            return new HeaderViewHolder(view);
        }

        // TYPE_TRANSACTION
        View view = LayoutInflater.from(context)
                .inflate(R.layout.mockup_item_transaction_entry, parent, false);
        return new TransactionViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof AdHolder) {

            FrameLayout bannerContainer = ((AdHolder) holder).container;

            // 🔥 ALWAYS reset for RecyclerView reuse
            bannerContainer.removeAllViews();
            bannerContainer.setVisibility(View.GONE);

            // 🔥 Ads config may not be ready yet
            if (trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig() == null) {
                return;
            }

            PriorityBannerController.show(
                    (Activity) context,
                    bannerContainer,
                    trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig()
            );

            return;
        }


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

            h.tvIncomeTotal.setText(CurrencyFormatterUtil.format(header.incomeTotal));
            h.tvExpenseTotal.setText(CurrencyFormatterUtil.format(header.expenseTotal));
            h.tvIncomeTotal.setTextColor(Color.parseColor("#388E3C")); // darker green
            h.tvExpenseTotal.setTextColor(Color.parseColor("#DA6C6C")); // darker red


        } else if (holder instanceof TransactionViewHolder) {
            Transaction transaction = (Transaction) item;
            TransactionViewHolder t = (TransactionViewHolder) holder;

            t.tvAmount.setText(CurrencyFormatterUtil.format(transaction.amount));

            t.tvDate.setText(sdf.format(transaction.date));

            // Inside TransactionGroupedAdapter
            t.contentLayout.setOnClickListener(v -> {

                if (!(item instanceof Transaction)) return;

                Transaction txn = (Transaction) item;

                // ✅ If this is a Transfer transaction → SHOW BLOCKING DIALOG
                if ("TransferDebit".equalsIgnoreCase(txn.type) ||
                        "TransferCredit".equalsIgnoreCase(txn.type)) {

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                            .setTitle("Transfer Transaction")
                            .setMessage("Transfer transactions cannot be edited because they are linked.\n\n" +
                                    "If you want to modify, please delete and recreate the transfer.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true)
                            .show();
                    return; // ✅ stop here
                }

                // ✅ Normal flow: open edit screen
                Intent intent = new Intent(context, AddTransactionActivity.class);
                intent.putExtra("transaction_id", txn.transaction_id);
                context.startActivity(intent);
            });


            // ✅ Show recurring icon only if recurring_id is NOT null
            if (transaction.recurring_id != null && transaction.recurring_id > 0) {
                t.ivRecurringIcon.setVisibility(View.VISIBLE);
            } else {
                t.ivRecurringIcon.setVisibility(View.GONE);
            }


            if (transaction.source_name != null && !transaction.source_name.trim().isEmpty()) {
                t.tvSourceName.setVisibility(View.VISIBLE);
                t.tvSourceName.setText(transaction.source_name);

            } else {
                t.tvSourceName.setVisibility(View.GONE);

            }


            // Notes
            if (transaction.notes != null && !transaction.notes.trim().isEmpty()) {
                t.tvNotes.setVisibility(View.VISIBLE);
                t.tvNotes.setText(transaction.notes);

                t.hr.setVisibility(View.VISIBLE);

            } else {
                t.tvNotes.setVisibility(View.GONE);
                t.hr.setVisibility(View.GONE);

            }

            // ✅ Amount Icon (Income/Expense)
            if ("Income".equalsIgnoreCase(transaction.type)) {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(Color.parseColor("#C8E6C9")); // Light green
                t.bgIconAmount.setBackground(bg);

            } else if ("Expense".equalsIgnoreCase(transaction.type)) {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(Color.parseColor("#FFCDD2")); // Light red
                t.bgIconAmount.setBackground(bg);

            } else if ("TransferDebit".equalsIgnoreCase(transaction.type) ||
                    "TransferCredit".equalsIgnoreCase(transaction.type)) {

                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);

                if ("TransferDebit".equalsIgnoreCase(transaction.type)) {
                    // 🔻 Transfer Debit (Money going out)
                    bg.setColor(Color.parseColor("#FFCDD2")); // RED BG

                } else {
                    // 🔺 Transfer Credit (Money coming in)
                    bg.setColor(Color.parseColor("#C8E6C9")); // GREEN BG

                }

                t.bgIconAmount.setBackground(bg);
            } else {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                bg.setColor(Color.parseColor("#FFFFFF"));
                t.bgIconAmount.setBackground(bg);
            }


            SwipeRevealHelper.attach(
                    t.itemView.findViewById(R.id.contentLayout),
                    t.itemView.findViewById(R.id.btnDeleteBackground),
                    () -> {

                        // ✅ If transaction is a Transfer → show confirmation dialog first
                        if (transaction.transfer_group_id != null && transaction.transfer_group_id > 0) {

                            ((Activity) context).runOnUiThread(() -> {

                                new MaterialAlertDialogBuilder(context)
                                        .setTitle("Delete Transfer?")
                                        .setMessage("This transfer contains 2 linked transactions (Debit & Credit).\nDeleting 1 will delete the other automatically.\n\nDo you want to continue?")
                                        .setPositiveButton("Delete", (d, w) -> {

                                            Executors.newSingleThreadExecutor().execute(() -> {

                                                // ✅ Fetch linked transactions (Debit + Credit)
                                                List<Transaction> linked = db.transactionDao()
                                                        .getTransactionsByTransferGroupId(transaction.transfer_group_id);

                                                for (Transaction tr : linked) {

                                                    // ✅ Reverse account balance BEFORE delete
                                                    if (tr.subtype_id != null) {

                                                        long accountId = db.subtypeDao().getAccountIdBySubtypeId(tr.subtype_id);

                                                        double reverseValue = 0;

                                                        if ("TransferDebit".equalsIgnoreCase(tr.type)) {
                                                            reverseValue = tr.amount; // amount is already negative → add (undo)
                                                        } else if ("TransferCredit".equalsIgnoreCase(tr.type)) {
                                                            reverseValue = -tr.amount; // amount is positive → subtract (undo)
                                                        }

                                                        db.accountDao().updateBalance(accountId, reverseValue);
                                                    }

                                                    db.transactionDao().delete(tr);
                                                }

                                                // ✅ Update UI
                                                ((Activity) context).runOnUiThread(() -> {
                                                    int pos = holder.getBindingAdapterPosition();
                                                    if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
                                                        removeAt(pos);
                                                        notifyItemRangeChanged(pos, items.size() - pos);
                                                    }
                                                });

                                            });

                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            });

                            return;  // ✅ Prevent normal delete logic from running
                        }


                        // ✅ NORMAL delete (Income / Expense) — no dialog
                        Executors.newSingleThreadExecutor().execute(() -> {

                            long accountId = db.subtypeDao().getAccountIdBySubtypeId(transaction.subtype_id);

                            double reverseValue = 0;

                            if ("Income".equalsIgnoreCase(transaction.type)) {
                                reverseValue = -transaction.amount;  // undo credit
                            } else if ("Expense".equalsIgnoreCase(transaction.type)) {
                                reverseValue = transaction.amount;   // undo debit
                            }

                            db.accountDao().updateBalance(accountId, reverseValue);

                            db.transactionDao().delete(transaction);

                            ((Activity) context).runOnUiThread(() -> {
                                int pos = holder.getBindingAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
                                    removeAt(pos);
                                    notifyItemRangeChanged(pos, items.size() - pos);
                                }
                            });
                        });

                    }
            );




            // Category
            if (transaction.category_id != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Category category = db.categoryDao().getCategoryById(transaction.category_id);
                    if (category != null) {
                        int resId = context.getResources().getIdentifier(
                                category.icon, "drawable", context.getPackageName());

                        ((Activity) context).runOnUiThread(() -> {
                            t.tvCategoryName.setText(category.name);
                            t.ivCategoryIcon.setImageResource(resId);

                            int baseColor = Color.parseColor(category.colorHex);

                            // ✅ Background pastel
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(baseColor);
                            t.bgIconCategory.setBackground(bg);

                            // ✅ Darker icon
                            int darkerColor = manipulateColor(baseColor, 0.5f);
                            t.ivCategoryIcon.setColorFilter(darkerColor);
                        });
                    }
                });
            }

            // Subtype
            if (transaction.subtype_id != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Subtype subtype = db.subtypeDao().getSubtypeById(transaction.subtype_id);
                    if (subtype != null) {
                        int resId = context.getResources().getIdentifier(
                                subtype.icon, "drawable", context.getPackageName());

                        ((Activity) context).runOnUiThread(() -> {
                            t.tvSubtypeName.setText(subtype.name);
                            t.ivSubtypeIcon.setImageResource(resId);

                            int baseColor = Color.parseColor(subtype.backgroundColorHex);

                            // ✅ Background pastel
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(baseColor);
                            t.bgIconSubType.setBackground(bg);

                            // ✅ Darker icon
                            int darkerColor = manipulateColor(baseColor, 0.6f);
                            t.ivSubtypeIcon.setColorFilter(darkerColor);
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

    static class AdHolder extends RecyclerView.ViewHolder {
        FrameLayout container;
        public AdHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.banner_container);
        }
    }


    // TRANSACTION HOLDER
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSubtypeName, tvSourceName, tvNotes, tvAmount, tvDate;
        ImageView ivCategoryIcon, ivSubtypeIcon, ivAmountIcon, ivRecurringIcon;
        FrameLayout bgIconCategory, bgIconSubType, bgIconAmount;
        View hr;
        LinearLayout contentLayout;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            hr = itemView.findViewById(R.id.hr);
            //ivAmountIcon = itemView.findViewById(R.id.ivAmountIcon);
            bgIconCategory = itemView.findViewById(R.id.bgIconCategory);
            bgIconSubType = itemView.findViewById(R.id.bgIconSubType);
            bgIconAmount = itemView.findViewById(R.id.bgIconAmount);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
            tvSourceName = itemView.findViewById(R.id.tvSourceName);
            ivRecurringIcon = itemView.findViewById(R.id.ivRecurringIcon);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }



    public void updateData(List<Object> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged(); // 🔥 can replace with DiffUtil for smooth animations
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

    public Object getItem(int position) {
        return items.get(position);
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }


}
