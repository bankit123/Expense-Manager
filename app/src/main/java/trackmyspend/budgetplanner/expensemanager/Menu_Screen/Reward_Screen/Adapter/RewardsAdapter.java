package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import trackmyspend.budgetplanner.expensemanager.R;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.VH> {

    public interface Listener {
        void onDirectActionClicked(RewardModel model);
    }

    private ArrayList<RewardModel> items;
    private final Listener listener;

    public RewardsAdapter(ArrayList<RewardModel> items, Listener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RewardModel r = items.get(position);

        holder.title.setText(r.title != null ? r.title : "");
        holder.points.setText(r.pointsEarned + " pts");

        holder.action.setText(r.buttonText != null ? r.buttonText : "Claim");

        holder.action.setOnClickListener(v -> {
            if (listener != null) listener.onDirectActionClicked(r);
        });

        // Hide divider for last item
        if (position == items.size() - 1) {
            if (holder.divider != null) holder.divider.setVisibility(View.GONE);
        } else {
            if (holder.divider != null) holder.divider.setVisibility(View.VISIBLE);
        }

        holder.icon.setImageResource(R.drawable.ic_gift); // or change per type
    }

    public void updateList(ArrayList<RewardModel> newList) {
        if (newList == null) {
            this.items.clear();
        } else {
            this.items = new ArrayList<>(newList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, points, desc;
        TextView action;
        View divider;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgGift);
            title = itemView.findViewById(R.id.tvRewardTitle);
            points = itemView.findViewById(R.id.tvRewardPoints);
            action = itemView.findViewById(R.id.btnClaim);
            divider = itemView.findViewById(R.id.viewDivider);
        }
    }
}
