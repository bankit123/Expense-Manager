package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter;

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

import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.R;

/**
 * 🔹 Simple local adapter used inside Add_Account_Activity
 * Only displays subtypes (no click handling, no external dependencies)
 */
public class SubtypePreviewAdapter extends RecyclerView.Adapter<SubtypePreviewAdapter.ViewHolder> {

    private final Context context;
    private final List<Subtype> subtypes;

    public SubtypePreviewAdapter(Context context, List<Subtype> subtypes) {
        this.context = context;
        this.subtypes = subtypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subtype_child_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subtype s = subtypes.get(position);

        holder.tvSubtypeName.setText(s.name);

        int resId = context.getResources().getIdentifier(s.icon, "drawable", context.getPackageName());
        if (resId != 0) holder.ivSubtypeIcon.setImageResource(resId);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        try {
            bg.setColor(Color.parseColor(s.backgroundColorHex != null ? s.backgroundColorHex : "#E7E7E7"));
        } catch (Exception e) {
            bg.setColor(Color.parseColor("#E7E7E7"));
        }
        holder.bgSubtypeIcon.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return subtypes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubtypeName;
        ImageView ivSubtypeIcon;
        FrameLayout bgSubtypeIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubtypeName = itemView.findViewById(R.id.tvSubtypeName);
            ivSubtypeIcon = itemView.findViewById(R.id.ivSubtypeIcon);
            bgSubtypeIcon = itemView.findViewById(R.id.bgSubtypeIcon);
        }
    }
}

