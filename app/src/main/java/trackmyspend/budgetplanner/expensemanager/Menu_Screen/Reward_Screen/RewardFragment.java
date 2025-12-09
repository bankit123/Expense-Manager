package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.GameModel;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.GamesAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.RewardModel;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.RewardsAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

public class RewardFragment extends Fragment {

    private RecyclerView rv;
    private RewardsAdapter rewardsAdapter;
    private GamesAdapter gamesAdapter;

    // master lists (data from server)
    private final ArrayList<RewardModel> rewardList = new ArrayList<>();
    private final ArrayList<GameModel> gameList = new ArrayList<>();

    private RequestQueue queue;

    // Replace with your actual URLs
    private static final String REWARDS_URL = "https://bankit123.github.io/wallpaper/trackreward.json";
    private static final String GAME_URL = "https://bankit123.github.io/wallpaper/trackgame.json";

    // tag for volley requests so we can cancel them
    private static final String REQ_TAG = "REWARDS_REQ";

    // "direct" or "game"
    private String currentMode = "direct";

    public RewardFragment() { /* required empty ctor */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reward, container, false);

        TextView tvDirect = view.findViewById(R.id.tvWeekly);   // Direct Points tab
        TextView tvGames = view.findViewById(R.id.tvMonthly);   // Games tab

        rv = view.findViewById(R.id.rewards_list);

        // default: vertical list for direct points
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // adapters - supply simple click handlers
        rewardsAdapter = new RewardsAdapter(new ArrayList<>(), this::onRewardClick);
        gamesAdapter = new GamesAdapter(new ArrayList<>(), this::onGameClick);

        rv.setAdapter(rewardsAdapter);

        queue = Volley.newRequestQueue(requireContext());

        // default selection
        setFilterSelected(tvDirect, tvGames);
        currentMode = "direct";

        // tab listeners
        tvDirect.setOnClickListener(v -> {
            currentMode = "direct";
            // ensure list layout
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(rewardsAdapter);
            setFilterSelected(tvDirect, tvGames);
            // show cached list if already fetched, otherwise fetch is already triggered below
            rewardsAdapter.updateList(new ArrayList<>(rewardList));
        });

        tvGames.setOnClickListener(v -> {
            currentMode = "game";
            // grid 3 columns
            rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            rv.setAdapter(gamesAdapter);
            setFilterSelected(tvGames, tvDirect);
            // fetch games (or show cached)
            if (gameList.isEmpty()) fetchGames();
            else gamesAdapter.updateList(new ArrayList<>(gameList));
        });

        // fetch rewards initially (games fetched only on demand)
        fetchRewards();

        return view;
    }

    // ---------- fetch rewards JSON ----------
    private void fetchRewards() {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, REWARDS_URL, null,
                response -> {
                    try {
                        rewardList.clear();
                        JSONArray arr = response.optJSONArray("rewards");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                RewardModel r = new RewardModel();
                                r.id = o.optInt("id");
                                r.title = o.optString("title");
                                r.description = o.optString("description");
                                r.pointsEarned = o.optInt("pointsEarned", 0);
                                r.buttonText = o.optString("buttonText");
                                r.rewardType = o.optString("rewardType");
                                r.actionName = o.optString("actionName");
                                rewardList.add(r);
                            }
                        }
                        // update adapter only if currently in direct mode
                        if ("direct".equals(currentMode)) {
                            rewardsAdapter.updateList(new ArrayList<>(rewardList));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Network error loading rewards", Toast.LENGTH_SHORT).show();
                });

        req.setTag(REQ_TAG);
        queue.add(req);
    }

    // ---------- fetch games JSON ----------
    private void fetchGames() {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, GAME_URL, null,
                response -> {
                    try {
                        gameList.clear();
                        JSONArray arr = response.optJSONArray("games");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                GameModel g = new GameModel();
                                g.id = o.optInt("id");
                                g.name = o.optString("name");
                                g.icon = o.optString("icon");           // image url
                                g.buttonText = o.optString("buttonText");
                                g.pointsEarned = o.optInt("pointsEarned", 0);
                                g.actionName = o.optString("actionName");
                                gameList.add(g);
                            }
                        }
                        // if grid active show it
                        if ("game".equals(currentMode)) {
                            gamesAdapter.updateList(new ArrayList<>(gameList));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Parse error (games)", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Network error loading games", Toast.LENGTH_SHORT).show();
                });

        req.setTag(REQ_TAG);
        queue.add(req);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (queue != null) queue.cancelAll(REQ_TAG);
    }

    // set selected tab visuals (selected / others)
    private void setFilterSelected(TextView selected, TextView other) {
        int active = getResources().getColor(R.color.nav_icon_active);
        int normal = getResources().getColor(R.color.nav_icon_default);

        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(active);

        other.setBackgroundResource(R.drawable.bg_segment_unselected);
        other.setTextColor(normal);
    }

    // click handlers from adapters
    private void onRewardClick(RewardModel r) {
        Toast.makeText(requireContext(), "Reward clicked: " + r.title, Toast.LENGTH_SHORT).show();
        // TODO: handle reward action (show rewarded ad etc.)
    }

    private void onGameClick(GameModel g) {
        Toast.makeText(requireContext(), "Play game: " + g.name, Toast.LENGTH_SHORT).show();
        // TODO: open game url or launch game activity
    }
}
