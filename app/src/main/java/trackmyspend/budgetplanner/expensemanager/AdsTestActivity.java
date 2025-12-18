package trackmyspend.budgetplanner.expensemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.nativead.NativeAdView;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleInterstitialAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleRewardedAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityInterstitialController;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityNativeController;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityRewardedController;

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
        Button btnNative = findViewById(R.id.btnNative);
        LayoutInflater inflater = LayoutInflater.from(this);

// 🔵 Google Native
        NativeAdView googleNativeAdView =
                (NativeAdView) LayoutInflater.from(this)
                        .inflate(R.layout.native_ad_google_video, null);


// 🔵 Facebook Native
        NativeAdLayout facebookNativeLayout =
                (NativeAdLayout) inflater.inflate(
                        R.layout.native_ad_facebook,
                        null
                );


        btnNative.setOnClickListener(v ->{
            FrameLayout nativeContainer = findViewById(R.id.nativeContainer);

            PriorityNativeController.show(
                    this,
                    nativeContainer,
                    AdsManager.getConfig(),
                    googleNativeAdView,   // your NativeAdView
                    facebookNativeLayout  // your NativeAdLayout
            );

        });

        // 🟨 Banner
        btnBanner.setOnClickListener(v -> {

            if (AdsManager.getConfig() == null) {
                Toast.makeText(
                        AdsTestActivity.this,
                        "Ads not ready yet",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            FrameLayout bannerContainer = findViewById(R.id.bannerContainer);

            PriorityBannerController.show(
                    AdsTestActivity.this,
                    bannerContainer,
                    AdsManager.getConfig()
            );
        });


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
