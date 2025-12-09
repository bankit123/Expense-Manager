package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import trackmyspend.budgetplanner.expensemanager.R;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.VH> {

    public interface Listener {
        void onGameClicked(GameModel model);
    }

    private ArrayList<GameModel> items;
    private final Listener listener;

    public GamesAdapter(ArrayList<GameModel> items, Listener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GameModel g = items.get(position);

        holder.title.setText(g.name);
        holder.points.setText(g.pointsEarned + " pts");
        holder.action.setText(g.buttonText);

        // Load icon using Picasso/Glide
        Picasso.get().load(g.icon).placeholder(R.drawable.logo).into(holder.icon);

        holder.action.setOnClickListener(v -> {
            if (listener != null) listener.onGameClicked(g);
        });

        holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    public void updateList(ArrayList<GameModel> list) {
        items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, points;
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

