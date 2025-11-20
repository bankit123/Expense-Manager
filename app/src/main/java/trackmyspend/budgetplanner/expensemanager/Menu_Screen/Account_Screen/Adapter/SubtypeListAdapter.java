package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.R;

public class SubtypeListAdapter extends RecyclerView.Adapter<SubtypeListAdapter.ViewHolder> {

    private final List<Subtype> subtypes;
    private final Set<Long> selectedIds;
    private final Context context;

    public SubtypeListAdapter(Context context, List<Subtype> subtypes, Set<Long> selectedIds) {
        this.context = context;
        this.subtypes = subtypes;
        this.selectedIds = new HashSet<>(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_subtype_checkbox_accountdetails, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subtype subtype = subtypes.get(position);
        holder.tvName.setText(subtype.name);

        // Load icon safely
        int iconRes = context.getResources().getIdentifier(
                subtype.icon != null ? subtype.icon : "",
                "drawable",
                context.getPackageName()
        );
        if (iconRes != 0) holder.ivSubtypeIcon.setImageResource(iconRes);
        else holder.ivSubtypeIcon.setImageResource(R.drawable.ic_category);

        boolean isSelected = selectedIds.contains(subtype.subtype_id);
        updateToggleUIAnimated(holder.itemView, holder.tvName, holder.ivSubtypeIcon, isSelected, false);

        holder.itemView.setOnClickListener(v -> {
            toggleSelection(subtype.subtype_id);
            boolean nowSelected = selectedIds.contains(subtype.subtype_id);
            updateToggleUIAnimated(holder.itemView, holder.tvName, holder.ivSubtypeIcon, nowSelected, true);
        });
    }

    private void toggleSelection(long id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        notifyDataSetChanged();
    }

    /** 🔹 Updated: changes icon tint color when selected */
    private void updateToggleUIAnimated(View view, TextView label, ImageView icon, boolean selected, boolean animate) {
        int selectedColor = context.getColor(R.color.nav_icon_active);
        int unselectedColor = context.getColor(R.color.opposite_color);
        int bgSelected = context.getColor(R.color.toggle_selected_bg); // Light blue (define in colors.xml)
        int bgUnselected = context.getColor(android.R.color.transparent);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(70f);
        bg.setStroke(selected ? 3 : 2, selected ? selectedColor : unselectedColor); // ✅ selected = 3dp, unselected = 2dp
        bg.setColor(selected ? bgSelected : bgUnselected);

        // ✅ Change icon tint on select/deselect
        icon.setColorFilter(selected ? selectedColor : unselectedColor, PorterDuff.Mode.SRC_IN);

        if (animate) {
            view.animate()
                    .scaleX(selected ? 1.05f : 1f)
                    .scaleY(selected ? 1.05f : 1f)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(150)
                    .start();
        }

        label.setTextColor(selected ? selectedColor : unselectedColor);
        view.setBackground(bg);
    }


    public Set<Long> getSelectedSubtypeIds() {
        return selectedIds;
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return subtypes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivSubtypeIcon;
        FrameLayout bgIconSubtype;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            tvName = itemView.findViewById(R.id.tvSubtypeName);
        }
    }

    private String getRandomPastelColor() {
        Random random = new Random();
        float hue = random.nextInt(360);
        float saturation = 0.25f + random.nextFloat() * 0.2f;
        float value = 0.95f;
        int color = Color.HSVToColor(new float[]{hue, saturation, value});
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
