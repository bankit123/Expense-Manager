package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.entities.ColorEntity;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private final List<ColorEntity> colorList;
    private final OnColorClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // track selection

    public interface OnColorClickListener {
        void onColorClick(ColorEntity color);
    }

    public ColorAdapter(List<ColorEntity> colorList, OnColorClickListener listener) {
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mockup_item_color, parent, false);

        // calculate perfect square size for 6 items in a row
        int spanCount = 10;
        int spacing = (int) (8 * parent.getResources().getDisplayMetrics().density); // 8dp
        int screenWidth = parent.getResources().getDisplayMetrics().widthPixels;

        int totalSpacing = spacing * (spanCount + 1); // include left+right margins
        int size = (screenWidth - totalSpacing) / spanCount;

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
        lp.setMargins(spacing, spacing, spacing, spacing);
        v.setLayoutParams(lp);

        return new ColorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ColorEntity colorEntity = colorList.get(position);

        // set background tint (circle color)
        holder.viewColor.getBackground().setTint(Color.parseColor(colorEntity.hex));

        // handle selection state
        holder.viewSelection.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // notify both old and new items to redraw
            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            listener.onColorClick(colorEntity);
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        View viewSelection;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
            viewSelection = itemView.findViewById(R.id.viewSelection);
        }
    }
}