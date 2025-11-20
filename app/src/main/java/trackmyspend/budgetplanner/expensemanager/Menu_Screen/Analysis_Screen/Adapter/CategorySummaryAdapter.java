package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Analysis_Screen.Adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.Graph.CategorySummary;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder> {

    private final List<CategorySummary> items = new ArrayList<>();
    private double totalAmount = 0;

    public void setData(List<CategorySummary> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);

            // calculate total
            totalAmount = 0;
            for (CategorySummary s : data) {
                totalAmount += s.totalAmount;
            }

            // sort by highest amount
            items.sort((a, b) -> Double.compare(b.totalAmount, a.totalAmount));
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_summary_analysis, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategorySummary item = items.get(position);

        // Category name
        holder.name.setText(item.categoryName != null ? item.categoryName : "Other");

        // Percentage
        double percent = totalAmount > 0 ? (item.totalAmount / totalAmount) * 100 : 0;
        holder.percent.setText(String.format(Locale.getDefault(), "%.1f%%", percent));

        // Amount
        //holder.amount.setText("₹" + String.format(Locale.getDefault(), "%.2f", item.totalAmount));
// Amount with user’s currency + locale formatting
        holder.amount.setText(CurrencyFormatterUtil.format(item.totalAmount));

        // Load drawable icon
        int resId = 0;
        if (item.categoryIcon != null) {
            resId = holder.itemView.getContext().getResources()
                    .getIdentifier(item.categoryIcon, "drawable", holder.itemView.getContext().getPackageName());
        }
        if (resId != 0) {
            holder.iconImage.setImageResource(resId);
        } else {
            holder.iconImage.setImageResource(R.drawable.ic_category); // fallback
        }

        // ✅ Circular background with colorHex
        String hex = (item.colorHex != null && !item.colorHex.isEmpty()) ? item.colorHex : "#BDBDBD";
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor(hex));
        holder.iconBackground.setBackground(bg);

        // ✅ Darker tint for the icon itself
        int baseColor = Color.parseColor(hex);
        int darkerColor = manipulateColor(baseColor, 0.6f);
        holder.iconImage.setColorFilter(darkerColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconBackground;
        ImageView iconImage;
        TextView name, percent, amount;

        ViewHolder(View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconImage = itemView.findViewById(R.id.iconImage);
            name = itemView.findViewById(R.id.tvCategoryName);
            percent = itemView.findViewById(R.id.tvCategoryPercent);
            amount = itemView.findViewById(R.id.tvCategoryAmount);
        }
    }

    // ✅ brighten/darken util
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
