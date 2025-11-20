package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Account_Details_Activity;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final Context context;
    private final List<Account> accountList;
    private final AppDatabase db;

    public AccountAdapter(Context context, List<Account> accountList) {
        this.context = context;
        this.accountList = accountList;
        this.db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mockup_item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);

        holder.tvAccountName.setText(account.name);
        holder.tvAccountAmount.setText(CurrencyFormatterUtil.format(account.amount));

        // Set account icon
        int resId = context.getResources().getIdentifier(account.icon, "drawable", context.getPackageName());
        if (resId != 0) holder.ivAccountIcon.setImageResource(resId);

        // Set background color
        if (account.iconColorHex != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(account.iconColorHex));
            holder.ivAccountIcon.setBackground(bg);

            int baseColor = Color.parseColor(account.iconColorHex);
            int darkerColor = manipulateColor(baseColor, 0.6f);
            holder.ivAccountIcon.setColorFilter(darkerColor);
        }

        // ✅ Click → open account details
        holder.contentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, Account_Details_Activity.class);
            intent.putExtra("accountId", account.account_id);
            intent.putExtra("accountName", account.name);
            context.startActivity(intent);
        });

        SwipeRevealHelper.attach(
                holder.itemView.findViewById(R.id.contentLayout),
                holder.itemView.findViewById(R.id.btnDeleteBackground),
                () -> {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Delete Account")
                            .setMessage("Are you sure you want to delete \"" + account.name +
                                    "\"?\nAll related subtypes and transactions will also be removed.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    // ✅ Directly call delete by ID (no assignment)
                                    db.accountDao().deleteAccountById(account.account_id);

                                    ((Activity) context).runOnUiThread(() -> {
                                        int pos = holder.getBindingAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION && pos < accountList.size()) {
                                            accountList.remove(pos);
                                            notifyItemRemoved(pos);
                                            notifyItemRangeChanged(pos, accountList.size() - pos);
                                        }
                                    });
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );

    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contentLayout;
        LinearLayout btnDeleteBackground;
        ImageView ivAccountIcon;
        TextView tvAccountName, tvAccountAmount, tvTransactionDetails;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            btnDeleteBackground = itemView.findViewById(R.id.btnDeleteBackground);
            ivAccountIcon = itemView.findViewById(R.id.ivAccountIcon);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountAmount = itemView.findViewById(R.id.tvAccountAmount);
            tvTransactionDetails = itemView.findViewById(R.id.tvTransactionDetails);
        }
    }

    // ✅ Utility for darker icon color
    private int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
