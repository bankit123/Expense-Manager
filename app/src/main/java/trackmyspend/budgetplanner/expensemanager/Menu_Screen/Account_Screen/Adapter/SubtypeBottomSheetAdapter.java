package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter;

import android.app.Activity;
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

import java.util.List;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.SwipeRevealHelper;

public class SubtypeBottomSheetAdapter extends RecyclerView.Adapter<SubtypeBottomSheetAdapter.ViewHolder> {

    private final List<Subtype> list;
    private final OnSubtypeClickListener listener;
    private final AppDatabase db;
    private final Activity activity;   // ✅ FIX: Safe reference

    public interface OnSubtypeClickListener {
        void onClick(Subtype subtype);
    }

    // 🔧 Constructor receives Activity safely
    public SubtypeBottomSheetAdapter(List<Subtype> list, OnSubtypeClickListener listener, Activity activity) {
        this.list = list;
        this.listener = listener;
        this.activity = activity;   // store activity
        this.db = AppDatabase.getDatabase(activity); // safe DB init
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtype_account_details_bottomsheet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subtype item = list.get(position);

        // Name + Info
        holder.tvSubtypeName.setText(item.name);
//        holder.tvSubtypeInfo.setText("Subtype ID: " + item.subtype_id);

        // 🎨 Load icon from drawable name
        if (item.icon != null && !item.icon.isEmpty()) {
            int iconRes = holder.itemView.getContext()
                    .getResources()
                    .getIdentifier(item.icon, "drawable",
                            holder.itemView.getContext().getPackageName());
            if (iconRes != 0) holder.ivIcon.setImageResource(iconRes);
        }

        // 🎨 OVAL background color
        try {
            int color = Color.parseColor(item.backgroundColorHex);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            holder.bgIcon.setBackground(bg);
        } catch (Exception ignored) {}

        // 🧭 Swipe-to-Delete
        SwipeRevealHelper.attach(
                holder.contentLayout,
                holder.btnDeleteBackground,
                () -> {
                    new MaterialAlertDialogBuilder(activity)
                            .setTitle("Delete Subtype")
                            .setMessage("Are you sure you want to delete \"" + item.name +
                                    "\"?\nAll related transactions will also be removed.")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                Executors.newSingleThreadExecutor().execute(() -> {

                                    Subtype st = db.subtypeDao().getSubtypeById(item.subtype_id);
                                    if (st != null) db.subtypeDao().deleteSubtype(st);

                                    activity.runOnUiThread(() -> {
                                        int pos = holder.getBindingAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION && pos < list.size()) {
                                            list.remove(pos);
                                            notifyItemRemoved(pos);
                                            notifyItemRangeChanged(pos, list.size() - pos);
                                        }
                                    });

                                });

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );

        // 🖱 Normal Click -> return selected subtype
//        holder.contentLayout.setOnClickListener(v -> {
//            if (listener != null) listener.onClick(item);
//        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ----------------------------------------------------
    // ViewHolder
    // ----------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout contentLayout;
        LinearLayout btnDeleteBackground;
        FrameLayout bgIcon;
        ImageView ivIcon;
        TextView tvSubtypeName, tvSubtypeInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contentLayout = itemView.findViewById(R.id.contentLayout);
            btnDeleteBackground = itemView.findViewById(R.id.btnDeleteBackground);
            bgIcon = itemView.findViewById(R.id.bgIcon);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
//            tvSubtypeInfo = itemView.findViewById(R.id.tvSubtypeInfo);
        }
    }
}
