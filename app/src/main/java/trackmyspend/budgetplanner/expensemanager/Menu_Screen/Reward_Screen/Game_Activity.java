package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.nativead.NativeAdView;


import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityNativeController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.NativeAdViewUtil;
import trackmyspend.budgetplanner.expensemanager.R;

public class Game_Activity extends AppCompatActivity {

    private static final int GAME_REQ = 2001;

    private long startTimeMillis = 0;
    private long totalTimeMillis = 0;
    private boolean isGameRunning = false;

    private String gameUrl = "";

    private TextView title_score, subtitle_score, points_score;
    private Button btn_play_again, btn_continue_exporing;

    private LottieAnimationView animationView1, animationView2;

    private AppDatabase db;
    private User currentUser;
    private int updatedPoints = 0;           // points awarded in this session
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

//        // Google Native
//        NativeAdView googleNativeAdView =
//                NativeAdViewUtil.createGoogleNative(this);
//
//// Facebook Native
//        NativeAdLayout facebookNativeLayout =
//                NativeAdViewUtil.createFacebookNative(this);
//
//
//        FrameLayout bannerContainer = findViewById(R.id.banner_container);
//        PriorityNativeController.show(
//                this,
//                bannerContainer,
//                AdsManager.getConfig(),
//                googleNativeAdView,   // your NativeAdView
//                facebookNativeLayout  // your NativeAdLayout
//        );

        FrameLayout bannerContainer = findViewById(R.id.banner_container);
        PriorityBannerController.show(
                this,
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_large")
        );



        // DB
        db = AppDatabase.getDatabase(this);

        // UI refs
        points_score = findViewById(R.id.points_score);
        title_score = findViewById(R.id.title_score);
        subtitle_score = findViewById(R.id.subtitle_score);
        btn_play_again = findViewById(R.id.btn_play_again);
        btn_continue_exporing = findViewById(R.id.btn_continue_exporing);
        animationView1 = findViewById(R.id.lottieAni1);
        animationView2 = findViewById(R.id.lottieAni2);

        // load user once (background)
        loadUserOnce();

        // read intent
        gameUrl = getIntent().getStringExtra("game_url");

        if (gameUrl == null || gameUrl.trim().isEmpty()) {
            finish();
            return;
        }

        // Launch game (custom tab). We rely on onActivityResult when user returns.
        launchGame(gameUrl);

        btn_continue_exporing.setOnClickListener(v -> {
            // Return earned points (0 if none)
            Intent result = new Intent();
            result.putExtra("updatedPoints", updatedPoints);
            // Also optionally return the new total in DB:
            int newTotal = currentUser != null ? currentUser.remaining_transaction_cnt : 0;
            result.putExtra("updatedTotal", newTotal);
            setResult(RESULT_OK, result);
            finish();
        });

        btn_play_again.setOnClickListener(v -> restartGame());
    }

    private void loadUserOnce() {
        exec.execute(() -> {
            try {
                currentUser = db.userDao().getFirstUser();
                if (currentUser == null) {
                    // create a minimal in-memory fallback (do not insert)
                    currentUser = new User();
                    currentUser.user_id = -1;
                    currentUser.remaining_transaction_cnt = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentUser = new User();
                currentUser.user_id = -1;
                currentUser.remaining_transaction_cnt = 0;
            }
        });
    }

    private void launchGame(String url) {
        try {
            CustomTabsIntent tab = new CustomTabsIntent.Builder().build();
            Intent intent = tab.intent;
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setData(Uri.parse(url));

            startTimeMillis = System.currentTimeMillis();
            isGameRunning = true;

            startActivityForResult(intent, GAME_REQ);
        } catch (Exception e) {
            e.printStackTrace();
            // If Custom Tabs fail, try default browser fallback
            try {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startTimeMillis = System.currentTimeMillis();
                isGameRunning = true;
                startActivityForResult(browser, GAME_REQ);
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }

    private void restartGame() {
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int req, int result, @Nullable Intent data) {
        super.onActivityResult(req, result, data);

        if (req == GAME_REQ && isGameRunning) {
            isGameRunning = false;

            totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
            long totalSec = Math.min(totalTimeMillis / 1000, 300);

            // NOTE: Random bonus removed — points = timeBonus only
            int timeBonus = getTimeBonus(totalSec);
            int totalPoints = timeBonus;

            // store awarded points locally and start DB update
            updatedPoints = totalPoints;

            Log.d("Game_Activity", "PlayTime: " + totalSec + " sec | Points: " + totalPoints);

            // Update UI immediately
            runOnUiThread(() -> {
                points_score.setText(String.valueOf(totalPoints));
                if (totalPoints == 0) {
                    title_score.setText("We Appreciate You!");
                    subtitle_score.setText("Support us to unlock rewards next time \uD83C\uDFAF");
                } else {
                    title_score.setText("Thank You!");
                    subtitle_score.setText("Your support helps us improve the app ⭐");
                }
            });

            if (totalPoints > 0) {
                updateDbAndRefreshUser(totalPoints);
                showAnimations();
            }
        }
    }

    // Update DB on background thread and refresh currentUser cached object
    private void updateDbAndRefreshUser(int pointsToAdd) {
        exec.execute(() -> {
            try {
                // ensure we have a real user id to update
                if (currentUser == null || currentUser.user_id <= 0) {
                    // attempt to fetch again from DB
                    currentUser = db.userDao().getFirstUser();
                }

                if (currentUser != null && currentUser.user_id > 0) {
                    db.userDao().addRemainingTransactions(currentUser.user_id, pointsToAdd);

                    // reload user's latest count
                    User refreshed = db.userDao().getFirstUser();
                    if (refreshed != null) currentUser = refreshed;
                } else {
                    // no user to credit — nothing to do
                    Log.w("Game_Activity", "No valid user found to credit points");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int getTimeBonus(long s) {
        // tuned to your desired scaling; adjust thresholds if needed
        if (s < 20) return 0;
        if (s < 30) return 2;
        if (s <= 60) return 3;
        if (s <= 120) return 4;
        if (s <= 180) return 5;
        if (s <= 240) return 6;
        return 7;
    }

    private void showAnimations() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animationView1.setVisibility(View.VISIBLE);
            animationView2.setVisibility(View.VISIBLE);
            animationView1.playAnimation();
            animationView2.playAnimation();

            // Safe vibration: check permission to avoid SecurityException
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null) {
                boolean hasVibratePermission =
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE)
                                == PackageManager.PERMISSION_GRANTED;

                if (hasVibratePermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        long[] pattern = {0, 100, 60, 150};
                        vib.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    } else {
                        vib.vibrate(300);
                    }
                } else {
                    // permission missing — skip vibration to avoid crash
                    Log.w("Game_Activity", "VIBRATE permission not granted, skipping vibration.");
                }
            }
        }, 400);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If the game is still running, calculate time and award if suitable (same logic as onActivityResult)
        if (isGameRunning) {
            isGameRunning = false;
            totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
            long totalSec = Math.min(totalTimeMillis / 1000, 300);

            int timeBonus = getTimeBonus(totalSec);
            int totalPoints = timeBonus;
            updatedPoints = totalPoints;

            if (totalPoints > 0) {
                // update DB then finish
                updateDbAndRefreshUser(totalPoints);
            }
        }

        // Return results even if zero
        Intent result = new Intent();
        result.putExtra("updatedPoints", updatedPoints);
        int newTotal = currentUser != null ? currentUser.remaining_transaction_cnt : 0;
        result.putExtra("updatedTotal", newTotal);
        setResult(RESULT_OK, result);

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exec.shutdownNow();
    }
}
