package trackmyspend.budgetplanner.expensemanager.Profile.Categories.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private final AppDatabase db;
    private RecyclerView recyclerView; // 🧭 we keep reference to block scrolling

    public CategoryAdapter(Context context, List<Category> categories, AppDatabase db) {
        this.context = context;
        this.categories = categories;
        this.db = db;
    }

    // 🧭 capture RecyclerView instance when attached
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_profile_with_delete, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        // 🏷️ Set category name
        holder.tvName.setText(category.name);
        holder.tvCategoryType.setText(category.type);

        // 🖼️ Set icon
        int resId = context.getResources().getIdentifier(category.icon, "drawable", context.getPackageName());
        if (resId != 0) holder.ivIcon.setImageResource(resId);

        // 🎨 Background color for icon
        try {
            int color = Color.parseColor(category.colorHex);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            holder.bgIcon.setBackground(bg);
        } catch (Exception ignored) {}

        // 🧭 Attach SwipeRevealHelper (same as Home)
        SwipeRevealHelper.attach(
                holder.itemView.findViewById(R.id.contentLayout),
                holder.itemView.findViewById(R.id.btnDeleteBackground),
                () -> {
                    // 🧭 Show confirmation dialog on delete
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                            .setTitle("Delete Category")
                            .setMessage("Are you sure you want to delete \"" + category.name + "\"?\nAll related transactions will also be removed.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    Category cat = db.categoryDao().getCategoryById(category.category_id);
                                    if (cat != null) {
                                        db.categoryDao().deleteCategory(cat);
                                    }

                                    ((Activity) context).runOnUiThread(() -> {
                                        int pos = holder.getBindingAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION && pos < categories.size()) {
                                            removeAt(pos);
                                            notifyItemRangeChanged(pos, categories.size() - pos);
                                        }
                                    });
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );


        // ✅ Disable RecyclerView scroll while swiping
        holder.itemView.setOnTouchListener((v, event) -> {
            if (recyclerView == null) return false;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // prevent parent from intercepting (start tracking)
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Disable scroll while finger is moving horizontally
                    recyclerView.requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Re-enable RecyclerView scrolling
                    recyclerView.requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false; // let SwipeRevealHelper still handle swipe
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategoryType;
        ImageView ivIcon;
        View bgIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryType = itemView.findViewById(R.id.tvCategoryType);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            bgIcon = itemView.findViewById(R.id.bgIconCategory);
        }
    }

    private void removeAt(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
    }
}
