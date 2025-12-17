package trackmyspend.budgetplanner.expensemanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AdView;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;
import trackmyspend.budgetplanner.expensemanager.AdManage.BannerAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleInterstitialAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleRewardedAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityInterstitialController;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityRewardedController;
import trackmyspend.budgetplanner.expensemanager.AdManage.RewardedAdHelper;

public class AdsTestActivity extends AppCompatActivity {

    private FrameLayout bannerContainer;
    private AdView fbAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ads_test);

        bannerContainer = findViewById(R.id.bannerContainer);
        Button btnBanner = findViewById(R.id.btnBanner);
        Button btnInterstitial = findViewById(R.id.btnInterstitial);
        Button btnReward = findViewById(R.id.btnReward);


        // 🟨 Banner
        btnBanner.setOnClickListener(v ->
                BannerAdHelper.show(
                        this,
                        bannerContainer,
                        AdsManager.getConfig(),
                        new BannerAdHelper.Callback() {

                            @Override
                            public void onAdMobLoaded() {
                                Toast.makeText(
                                        AdsTestActivity.this,
                                        "AdMob Banner Loaded",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onFacebookLoaded() {
                                Toast.makeText(
                                        AdsTestActivity.this,
                                        "Facebook Banner Loaded",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onAllFailed() {
                                Toast.makeText(
                                        AdsTestActivity.this,
                                        "Banner Failed (No Fill)",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
        );

        AdSettings.addTestDevice("1cfdec71-440b-467a-956b-40fe5ea023ec");

        // 🟥 Show interstitial according to Firebase priority
        btnInterstitial.setOnClickListener(v -> {

            if (AdsManager.getConfig() == null) {
                Toast.makeText(this, "Ads not ready yet", Toast.LENGTH_SHORT).show();
                return;
            }

            PriorityInterstitialController.show(
                    this,
                    AdsManager.getConfig(),
                    new GoogleInterstitialAdHelper.Callback() {

                        @Override
                        public void onShown() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Interstitial Shown",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onDismissed() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Interstitial Closed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "No Interstitial Available",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onNotReady() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Interstitial Not Ready",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });

        // 🟩 Rewarded
        btnReward.setOnClickListener(v -> {

            if (AdsManager.getConfig() == null) {
                Toast.makeText(
                        AdsTestActivity.this,
                        "Ads not ready yet",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            PriorityRewardedController.show(
                    AdsTestActivity.this,
                    AdsManager.getConfig(),
                    new GoogleRewardedAdHelper.Callback() {

                        @Override
                        public void onShown() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Rewarded Ad Shown",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onRewardEarned() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "🎁 Reward Earned!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // 👉 Give reward here
                            // e.g. addCoins(), unlockFeature(), etc.
                        }

                        @Override
                        public void onDismissed() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Rewarded Ad Closed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "No Rewarded Ad Available",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onNotReady() {
                            Toast.makeText(
                                    AdsTestActivity.this,
                                    "Rewarded Ad Not Ready",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });

    }
}
