package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                .inflate(R.layout.item_game, parent, false); // NEW GAME UI
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GameModel g = items.get(position);

        holder.title.setText(g.name);
        holder.points.setText(g.pointsEarned + " pts");

        // Load game image
        Picasso.get()
                .load(g.icon)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.icon);

        // Click whole card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGameClicked(g);
        });
    }

    public void updateList(ArrayList<GameModel> list) {
        items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, points;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgGame);
            title = itemView.findViewById(R.id.tvGameName);
            points = itemView.findViewById(R.id.tvGamePoints);
        }
    }
}
