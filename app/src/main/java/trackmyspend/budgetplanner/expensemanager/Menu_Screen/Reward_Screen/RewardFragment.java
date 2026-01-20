package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.GameModel;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.GamesAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.RewardModel;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen.Adapter.RewardsAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

public class RewardFragment extends Fragment {

    private RecyclerView rv;
    private RewardsAdapter rewardsAdapter;
    private GamesAdapter gamesAdapter;
    private TextView tvPoints;

    private final ArrayList<RewardModel> rewardList = new ArrayList<>();
    private final ArrayList<GameModel> gameList = new ArrayList<>();

    private RequestQueue queue;
    private AppDatabase db;
    private User currentUser;     // <-- Loaded once

    private static final String REWARDS_URL = "https://bankit123.github.io/wallpaper/trackreward.json";
    private static final String GAME_URL = "https://bankit123.github.io/wallpaper/trackgame.json";

    private String currentMode = "direct";
    private static final String REQ_TAG = "REWARDS_REQ";

    private ShimmerFrameLayout shimmerLayout;

    private static final int REQ_GAME_ACTIVITY = 3001;


    public RewardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reward, container, false);

        TextView tvDirect = view.findViewById(R.id.tvWeekly);
        TextView tvGames = view.findViewById(R.id.tvMonthly);
        tvPoints = view.findViewById(R.id.tv_points);
        rv = view.findViewById(R.id.rewards_list);

//        shimmerLayout = view.findViewById(R.id.shimmer_layout);
//        shimmerLayout.startShimmer();
//        rv.setVisibility(View.GONE);


        FrameLayout bannerContainer = view.findViewById(R.id.banner_container);
        PriorityBannerController.show(
                requireActivity(),
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );


        db = AppDatabase.getDatabase(requireContext());
        queue = Volley.newRequestQueue(requireContext());

        observeUserPoints();

        rewardsAdapter = new RewardsAdapter(new ArrayList<>(), this::onRewardClick);
        gamesAdapter = new GamesAdapter(new ArrayList<>(), this::onGameClick);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(rewardsAdapter);

        setFilterSelected(tvDirect, tvGames);

        tvDirect.setOnClickListener(v -> switchToDirect(tvDirect, tvGames));
        tvGames.setOnClickListener(v -> switchToGames(tvGames, tvDirect));

        fetchAllDataOnce();

        return view;
    }

    // ------------------- LOAD USER ONCE -------------------

    private void observeUserPoints() {
        db.userDao().getFirstUserLive().observe(getViewLifecycleOwner(), user -> {
            // This callback runs on main thread
            currentUser = user;
            int pts = (currentUser != null) ? currentUser.remaining_transaction_cnt : 0;
            tvPoints.setText(pts + " Times You Supported Us");
        });
    }

    // ------------------- FETCH EVERYTHING ONCE -------------------

    private void fetchAllDataOnce() {
        fetchRewards();
        fetchGames();
    }

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

                        if ("direct".equals(currentMode)) {
                            rewardsAdapter.updateList(new ArrayList<>(rewardList));
                        }

                    } catch (Exception ignored) {}
                }, error -> {});
        req.setTag(REQ_TAG);
        queue.add(req);
    }

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
                                g.url = o.optString("url");
                                g.icon = o.optString("imgUrl");
                                g.pointsEarned = o.optInt("points");
                                g.actionName = o.optString("actionName", "OPEN_GAME");
                                g.buttonText = o.optString("buttonText", "Play");

                                gameList.add(g);
                            }
                        }

                        if ("game".equals(currentMode)) {
                            gamesAdapter.updateList(new ArrayList<>(gameList));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Game load error", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show();
        });

        req.setTag(REQ_TAG);
        queue.add(req);
    }


    // ------------------- TAB SWITCH -------------------

    private void switchToDirect(TextView selected, TextView other) {
        currentMode = "direct";
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(rewardsAdapter);
        setFilterSelected(selected, other);
        rewardsAdapter.updateList(new ArrayList<>(rewardList));
    }

    private void switchToGames(TextView selected, TextView other) {
        currentMode = "game";
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rv.setAdapter(gamesAdapter);
        setFilterSelected(selected, other);
        gamesAdapter.updateList(new ArrayList<>(gameList));
    }

    private void setFilterSelected(TextView selected, TextView other) {
        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(getResources().getColor(R.color.nav_icon_active));

        other.setBackgroundResource(R.drawable.bg_segment_unselected);
        other.setTextColor(getResources().getColor(R.color.nav_icon_default));
    }

    // ------------------- ITEM CLICK HANDLERS -------------------

    private void onRewardClick(RewardModel r) {
        executeRewardAction(r);
    }

    // called from GamesAdapter click
    private void onGameClick(GameModel g) {
        if (g == null || g.url == null || g.url.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Game data invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Launch our Game_Activity (which will open custom tab and return points)
        try {
            Intent i = new Intent(requireContext(), Game_Activity.class);
            i.putExtra("game_url", g.url);
            // optional: pass expected points from JSON if you want the game activity to consider it
            i.putExtra("game_points", g.pointsEarned);
            startActivityForResult(i, REQ_GAME_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Unable to open game", Toast.LENGTH_SHORT).show();
        }
    }



    // ------------------- REFLECTION BASED ACTION CALL -------------------

    private void executeRewardAction(RewardModel r) {
        try {
            String functionName = r.actionName.trim();
            var method = RewardFragment.class.getDeclaredMethod(functionName, RewardModel.class);
            method.setAccessible(true);
            method.invoke(this, r);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid action: " + r.actionName, Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------- AD FUNCTIONS (CALLED VIA REFLECTION) -------------------

    /** ⭐ JSON: "actionName":"showRewardAd" */
    private void showRewardAd(RewardModel r) {

        if (trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig() == null) {
            Toast.makeText(
                    requireContext(),
                    "Ads not ready yet",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        trackmyspend.budgetplanner.expensemanager.AdManage.PriorityRewardedController.show(
                requireActivity(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                new trackmyspend.budgetplanner.expensemanager.AdManage.GoogleRewardedAdHelper.Callback() {

                    @Override
                    public void onShown() {
                        // optional: log or UI feedback
                    }

                    @Override
                    public void onRewardEarned() {
                        // ✅ GIVE REWARD HERE (NEW LOGIC)
                        int points = r.pointsEarned > 0 ? r.pointsEarned : 1;
                        updateUserPoints(points);
                    }

                    @Override
                    public void onDismissed() {
                        // no-op (reward already handled)
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(
                                requireContext(),
                                "No Rewarded Ad Available",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotReady() {
                        Toast.makeText(
                                requireContext(),
                                "Rewarded Ad Not Ready",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


// ------------------- AD FUNCTIONS (CALLED VIA REFLECTION) -------------------

    /** ⭐ JSON: "actionName":"showInterstitialAd" */
    private void showInterstitialAd(RewardModel r) {

        if (trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig() == null) {
            Toast.makeText(
                    requireContext(),
                    "Ads not ready yet",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int points = r.pointsEarned;

        trackmyspend.budgetplanner.expensemanager.AdManage.PriorityInterstitialController.show(
                requireActivity(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                new trackmyspend.budgetplanner.expensemanager.AdManage.GoogleInterstitialAdHelper.Callback() {

                    @Override
                    public void onShown() {
                        // optional: log
                    }

                    @Override
                    public void onDismissed() {
                        // ✅ GIVE REWARD ON CLOSE (NEW LOGIC)
                        updateUserPoints(points);
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(
                                requireContext(),
                                "No Interstitial Available",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotReady() {
                        Toast.makeText(
                                requireContext(),
                                "Interstitial Not Ready",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ------------------- UPDATE USER POINTS (ONE PLACE ONLY!) -------------------

    private void updateUserPoints(int add) {

        new Thread(() -> {
            if (currentUser == null) currentUser = db.userDao().getFirstUser();
            if (currentUser == null) return;

            long uid = currentUser.user_id;

            // Update database only (UI updates via LiveData)
            db.userDao().addRemainingTransactions(uid, add);

            // Local copy update (not strictly required but keeps object accurate)
            currentUser.remaining_transaction_cnt += add;

            // Optional: small toast feedback on main thread
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(),
                            "🎉 +" + add + " pts added!",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        queue.cancelAll(REQ_TAG);
    }
}
