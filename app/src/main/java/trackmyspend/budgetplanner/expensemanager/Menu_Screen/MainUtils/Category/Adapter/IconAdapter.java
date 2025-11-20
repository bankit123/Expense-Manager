package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Icon;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private final Context context;
    private final List<Icon> iconList;
    private final OnIconClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnIconClickListener {
        void onIconClick(Icon icon);
    }

    public IconAdapter(Context context, List<Icon> iconList, OnIconClickListener listener) {
        this.context = context;
        this.iconList = iconList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.mockup_item_icon, parent, false);

        int spanCount = 10; // same as GridLayoutManager
        int spacing = (int) (10 * parent.getResources().getDisplayMetrics().density); // 8dp spacing
        int screenWidth = parent.getResources().getDisplayMetrics().widthPixels;

        int totalSpacing = spacing * (spanCount + 1);
        int size = (screenWidth - totalSpacing) / spanCount;

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
        lp.setMargins(spacing, spacing, spacing, spacing);
        v.setLayoutParams(lp);

        return new IconViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        Icon icon = iconList.get(position);
        int resId = context.getResources().getIdentifier(icon.drawableName, "drawable", context.getPackageName());

        holder.ivIcon.setImageResource(resId);

        // selection handling (optional highlight)
//        holder.itemView.setAlpha(selectedPosition == position ? 1f : 0.6f);
//
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            listener.onIconClick(icon);
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
