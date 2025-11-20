package trackmyspend.budgetplanner.expensemanager.Profile.Subtype.Adapter;

import android.annotation.SuppressLint;
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

import java.util.List;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

public class AccountSubtypeGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_SUBTYPE = 1;

    private final Context context;
    private final List<Object> items;
    private final AppDatabase db;
    private int currentAccountColor = Color.parseColor("#FFFFFF");

    public AccountSubtypeGroupedAdapter(Context context, List<Object> items, AppDatabase db) {
        this.context = context;
        this.items = items;
        this.db = db;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof Account) ? TYPE_ACCOUNT : TYPE_SUBTYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACCOUNT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_account_with_subtypes_profile, parent, false);
            return new AccountViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_subtype_child_profile, parent, false);
            return new SubtypeViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        // 🏦 Account section
        if (holder instanceof AccountViewHolder) {
            Account account = (Account) item;
            AccountViewHolder a = (AccountViewHolder) holder;

            a.tvAccountName.setText(account.name);

            int resId = context.getResources().getIdentifier(account.icon, "drawable", context.getPackageName());
            a.ivAccountIcon.setImageResource(resId);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(account.iconColorHex != null ? account.iconColorHex : "#90CAF9"));
            a.bgIconAccount.setBackground(bg);

            // ✅ store color for subtypes
            currentAccountColor = Color.parseColor(account.iconColorHex != null ? account.iconColorHex : "#90CAF9");

            // 🧭 Swipe to delete Account
//            SwipeRevealHelper.attach(
//                    a.itemView.findViewById(R.id.contentLayout),
//                    a.itemView.findViewById(R.id.btnDeleteBackground),
//                    () -> {
//                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
//                                .setTitle("Delete Account?")
//                                .setMessage("Deleting this account will also remove all its subtypes and related transactions. Are you sure?")
//                                .setPositiveButton("Delete", (dialog, which) -> {
//                                    Executors.newSingleThreadExecutor().execute(() -> {
//                                        db.accountDao().deleteAccount(account);
//                                        ((Activity) context).runOnUiThread(() -> {
//                                            int pos = holder.getBindingAdapterPosition();
//                                            if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
//                                                removeAt(pos);
//                                                notifyItemRangeChanged(pos, items.size() - pos);
//                                            }
//                                        });
//                                    });
//                                })
//                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                                .show();
//                    }
//            );

            SwipeRevealHelper.attach(
                    a.itemView.findViewById(R.id.contentLayout),
                    a.itemView.findViewById(R.id.btnDeleteBackground),
                    () -> {

                        Executors.newSingleThreadExecutor().execute(() -> {

                            // 🔍 Check if ANY subtype under this account has transfer transaction
                            List<Subtype> subtypes = db.subtypeDao().getSubtypesByAccountId(account.account_id);

                            boolean hasTransferLinked = false;

                            for (Subtype st : subtypes) {
                                int count = db.transactionDao().countTransferTransactionsForSubtype(st.subtype_id);
                                if (count > 0) {
                                    hasTransferLinked = true;
                                    break;
                                }
                            }

                            boolean finalHasTransferLinked = hasTransferLinked;

                            ((Activity) context).runOnUiThread(() -> {

                                if (finalHasTransferLinked) {

                                    // ❌ Do NOT allow delete — block user
                                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                                            .setTitle("Cannot Delete Account")
                                            .setMessage("This account is used in Transfer transactions.\n\n" +
                                                    "First delete all transfer entries related to this account's payment method.")
                                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                            .show();

                                } else {

                                    // ✅ Normal delete confirmation
                                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                                            .setTitle("Delete Account?")
                                            .setMessage("Deleting this account will also remove all its subtypes and related transactions. Are you sure?")
                                            .setPositiveButton("Delete", (dialog, which) -> {
                                                Executors.newSingleThreadExecutor().execute(() -> {
                                                    db.accountDao().deleteAccount(account);
                                                    ((Activity) context).runOnUiThread(() -> {
                                                        int pos = holder.getBindingAdapterPosition();
                                                        if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
                                                            removeAt(pos);
                                                            notifyItemRangeChanged(pos, items.size() - pos);
                                                        }
                                                    });
                                                });
                                            })
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                            });
                        });
                    }
            );


        }

        // 🧩 Subtype section
        else if (holder instanceof SubtypeViewHolder) {
            Subtype subtype = (Subtype) item;
            SubtypeViewHolder s = (SubtypeViewHolder) holder;

            s.tvSubtypeName.setText(subtype.name);

            int resId = context.getResources().getIdentifier(subtype.icon, "drawable", context.getPackageName());
            s.ivSubtypeIcon.setImageResource(resId);

            // 🔵 Icon circle
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.OVAL);
            iconBg.setColor(Color.parseColor(subtype.backgroundColorHex != null ? subtype.backgroundColorHex : "#E0E0E0"));
            s.bgSubtypeIcon.setBackground(iconBg);

            // 🎨 Row background from parent account color
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(24f);
            rowBg.setColor(adjustAlpha(currentAccountColor, 0.16f));
            s.itemView.findViewById(R.id.contentLayout).setBackground(rowBg);

            // ✅ Check if this is the last subtype before the next account
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) s.itemView.getLayoutParams();

            boolean isLastSubtype =
                    (position == items.size() - 1) ||
                            (items.get(position + 1) instanceof Account);

            if (isLastSubtype) {
                params.bottomMargin = (int) (20 * context.getResources().getDisplayMetrics().density); // 16dp
            } else {
                params.bottomMargin = 0;
            }
            s.itemView.setLayoutParams(params);

            // Swipe delete
//            SwipeRevealHelper.attach(
//                    s.itemView.findViewById(R.id.contentLayout),
//                    s.itemView.findViewById(R.id.btnDeleteBackground),
//                    () -> {
//                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
//                                .setTitle("Delete Payment Method?")
//                                .setMessage("Deleting this payment method will also remove all related transactions. Are you sure?")
//                                .setPositiveButton("Delete", (dialog, which) -> {
//                                    Executors.newSingleThreadExecutor().execute(() -> {
//                                        db.subtypeDao().deleteSubtype(subtype);
//                                        ((Activity) context).runOnUiThread(() -> {
//                                            int pos = holder.getBindingAdapterPosition();
//                                            if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
//                                                removeAt(pos);
//                                                notifyItemRangeChanged(pos, items.size() - pos);
//                                            }
//                                        });
//                                    });
//                                })
//                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                                .show();
//                    }
//            );

            SwipeRevealHelper.attach(
                    s.itemView.findViewById(R.id.contentLayout),
                    s.itemView.findViewById(R.id.btnDeleteBackground),
                    () -> {

                        Executors.newSingleThreadExecutor().execute(() -> {

                            // 🔍 Check if this subtype is linked with ANY transfer
                            boolean hasTransfer = db.transactionDao()
                                    .hasTransferBySubtypeId(subtype.subtype_id);

                            ((Activity) context).runOnUiThread(() -> {

                                if (hasTransfer) {

                                    // ❌ Block delete — cannot delete subtype with transfer records
                                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                                            .setTitle("Cannot Delete Payment Method")
                                            .setMessage("This payment method is used in Transfer transactions.\n\n" +
                                                    "First delete all transfer entries related to this payment method.")
                                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                            .show();

                                } else {

                                    // ✅ Normal delete confirmation
                                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                                            .setTitle("Delete Payment Method?")
                                            .setMessage("Deleting this payment method will also remove all related transactions. Are you sure?")
                                            .setPositiveButton("Delete", (dialog, which) -> {
                                                Executors.newSingleThreadExecutor().execute(() -> {
                                                    db.subtypeDao().deleteSubtype(subtype);
                                                    ((Activity) context).runOnUiThread(() -> {
                                                        int pos = holder.getBindingAdapterPosition();
                                                        if (pos != RecyclerView.NO_POSITION && pos < items.size()) {
                                                            removeAt(pos);
                                                            notifyItemRangeChanged(pos, items.size() - pos);
                                                        }
                                                    });
                                                });
                                            })
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                            });
                        });
                    }
            );


        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName;
        ImageView ivAccountIcon;
        FrameLayout bgIconAccount;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            ivAccountIcon = itemView.findViewById(R.id.ivAccountIcon);
            bgIconAccount = itemView.findViewById(R.id.bgIconAccount);
        }
    }

    static class SubtypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubtypeName;
        ImageView ivSubtypeIcon;
        FrameLayout bgSubtypeIcon;

        public SubtypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            bgSubtypeIcon = itemView.findViewById(R.id.bgSubtypeIcon);
        }
    }

    public void updateData(List<Object> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
