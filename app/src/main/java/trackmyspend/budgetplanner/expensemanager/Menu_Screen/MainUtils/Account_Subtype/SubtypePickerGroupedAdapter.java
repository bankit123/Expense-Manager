package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Account_Subtype;

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

import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

public class SubtypePickerGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_SUBTYPE = 1;

    private final Context context;
    private final List<Object> items;
    private final OnSubtypeClickListener listener;

    // Remember the parent account color to softly tint its subtypes (optional)
    private int currentAccountColor = Color.parseColor("#E0E0E0");

    /** Listener for subtype clicks */
    public interface OnSubtypeClickListener {
        void onClick(Subtype subtype);
    }

    public SubtypePickerGroupedAdapter(Context context, List<Object> items, OnSubtypeClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof Account) ? TYPE_ACCOUNT : TYPE_SUBTYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACCOUNT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_account_header_picker, parent, false);
            return new AccountViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.mockup_item_subtype, parent, false);
            return new SubtypeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        // 🏦 Account header row
        if (holder instanceof AccountViewHolder) {
            Account account = (Account) item;
            AccountViewHolder a = (AccountViewHolder) holder;

            a.tvAccountName.setText(account.name);
            a.tvAccountAmount.setText(String.valueOf(CurrencyFormatterUtil.format(account.amount)));

            // Set icon

            int resId = context.getResources().getIdentifier(account.icon, "drawable", context.getPackageName());
            if (resId != 0) a.ivAccountIcon.setImageResource(resId);

            // Set background color
            String colorHex = (account.iconColorHex != null ? account.iconColorHex : "#90CAF9");
            int color = Color.parseColor(colorHex);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            a.bgIconAccount.setBackground(bg);

            currentAccountColor = color;
        }

        // 💼 Subtype item
        else if (holder instanceof SubtypeViewHolder) {
            Subtype subtype = (Subtype) item;
            SubtypeViewHolder s = (SubtypeViewHolder) holder;

            s.tvSubtypeName.setText(subtype.name);

            int resId = context.getResources().getIdentifier(subtype.icon, "drawable", context.getPackageName());
            if (resId != 0) s.ivSubtypeIcon.setImageResource(resId);

            // Background for icon circle
            String bgHex = (subtype.backgroundColorHex != null ? subtype.backgroundColorHex : "#E0E0E0");
            int baseColor = Color.parseColor(bgHex);

            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setShape(GradientDrawable.OVAL);
            iconBg.setColor(baseColor);
            s.bgSubtypeIcon.setBackground(iconBg);

            // Set darker tint for icon for better contrast
            int darkerColor = manipulateColor(baseColor, 0.6f);
            s.ivSubtypeIcon.setColorFilter(darkerColor);

            // Set click listener
            s.itemView.setOnClickListener(v -> listener.onClick(subtype));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 🧱 Account header ViewHolder
    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName, tvAccountAmount;
        ImageView ivAccountIcon;
        FrameLayout bgIconAccount;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountAmount = itemView.findViewById(R.id.tvAccountAmount);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            ivAccountIcon = itemView.findViewById(R.id.ivAccountIcon);
            bgIconAccount = itemView.findViewById(R.id.bgIconAccount);
        }
    }

    // 🧩 Subtype ViewHolder
    static class SubtypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubtypeName;
        ImageView ivSubtypeIcon;
        FrameLayout bgSubtypeIcon;

        public SubtypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            bgSubtypeIcon = itemView.findViewById(R.id.subtypeIconBackground); // ✅ Corrected ID
        }
    }

    // 🎨 Helper: darken or lighten a color
    private int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(
                a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255)
        );
    }
}
