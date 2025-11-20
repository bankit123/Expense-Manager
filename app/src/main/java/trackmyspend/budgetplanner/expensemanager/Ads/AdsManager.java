package trackmyspend.budgetplanner.expensemanager.Ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import trackmyspend.budgetplanner.expensemanager.R;

/**
 * 🚀 SmartAdsManager — Dynamic AdMob via Firebase + Safe Fallback
 * • Fetches ad unit IDs dynamically from Firebase
 * • Initializes AdMob SDK with Firebase appID
 * • Preloads Interstitial & Rewarded automatically
 * • Hides banner/native layouts until ready
 * • Includes callback + timeout + test fallback (no crashes)
 */
public class AdsManager {

    private static final String TAG = "SmartAdsManager";

    private static boolean isInitialized = false;
    private static final Map<String, String> adUnits = new HashMap<>();

    // Fallback Test Ad IDs (Google Official Test IDs)
    private static final Map<String, String> TEST_ADS = Map.of(
            "appID", "ca-app-pub-3940256099942544~33475117131",
            "banner", "ca-app-pub-3940256099942544/92145897411",
            "inter", "ca-app-pub-3940256099942544/103317371221",
            "reward", "ca-app-pub-3940256099942544/522435491721",
            "native", "ca-app-pub-3940256099942544/224769611021"
    );

    private static InterstitialAd interstitialAd;
    private static RewardedAd rewardedAd;
    private static NativeAd nativeAdInstance;

    // Interface for splash callback
    public interface InitCallback {
        void onInitialized();
    }

    // 🔹 Initialize Ads (with callback)
    public static void initialize(Context context, InitCallback callback) {
        if (isInitialized) {
            Log.d(TAG, "ℹ️ AdsManager already initialized");
            if (callback != null) callback.onInitialized();
            return;
        }

        Log.d(TAG, "🚀 Initializing SmartAdsManager...");
        isInitialized = true;

        // Safety timeout — fallback to test ads if Firebase is too slow
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (adUnits.isEmpty()) {
                Log.w(TAG, "⚠️ Firebase took too long — using default test ads");
                adUnits.putAll(TEST_ADS);
                initAdMob(context, TEST_ADS.get("appID"));
                if (callback != null) callback.onInitialized();
            }
        }, 5000); // 5 seconds timeout

        fetchAdIdsFromFirebase(context, callback);
    }

    // 🔹 Fetch AdMob IDs from Firebase
    private static void fetchAdIdsFromFirebase(Context context, InitCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ads");

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                adUnits.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    adUnits.put(snapshot.getKey(), snapshot.getValue(String.class));
                }

                Log.d(TAG, "✅ Firebase ad config loaded: " + adUnits);

                String appId = adUnits.get("appID");
                if (appId != null && !appId.trim().isEmpty()) {
                    initAdMob(context, appId);
                } else {
                    Log.w(TAG, "⚠️ Missing 'appID' in Firebase — using test ads");
                    adUnits.putAll(TEST_ADS);
                    initAdMob(context, TEST_ADS.get("appID"));
                }
            } else {
                Log.e(TAG, "❌ Firebase fetch failed — using test ads instead");
                adUnits.putAll(TEST_ADS);
                initAdMob(context, TEST_ADS.get("appID"));
            }

            if (callback != null) callback.onInitialized();
        });
    }

    // 🔹 Initialize AdMob SDK
    private static void initAdMob(Context context, String appId) {
        try {
            Log.d(TAG, "🧩 Initializing AdMob with App ID: " + appId);

            MobileAds.initialize(context, initializationStatus -> {
                logInitializationStatus(initializationStatus);
                preloadInterstitial(context);
                preloadRewarded(context);
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ AdMob initialization failed: " + e.getMessage());
        }
    }

    private static void logInitializationStatus(InitializationStatus status) {
        if (status != null) {
            for (String adapter : status.getAdapterStatusMap().keySet()) {
                Log.d(TAG, "Adapter: " + adapter + " → " +
                        status.getAdapterStatusMap().get(adapter).getDescription());
            }
        }
    }

    // ----------------------------
    // 🟦 Banner Ads
    // ----------------------------
    public static void loadBanner(Activity activity, FrameLayout container) {
        String adUnit = adUnits.get("banner");
        //Toast.makeText(activity, adUnit, Toast.LENGTH_SHORT).show();
        if (adUnit == null || adUnit.isEmpty()) {
            Log.w(TAG, "⚠️ Banner adUnit missing");
            container.setVisibility(View.GONE);
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnit);
//        Toast.makeText(activity, String.valueOf(adUnit), Toast.LENGTH_SHORT).show();


        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(View.GONE);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ Banner Ad Loaded");

//                  Toast.makeText(activity, "ad loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                container.setVisibility(View.GONE);
                Log.e(TAG, "❌ Banner failed: " + adError.getMessage());
                //Toast.makeText(activity, "ad not loaded", Toast.LENGTH_SHORT).show();

            }
        });

        adView.loadAd(adRequest);
    }

    public static void loadBanner(Activity activity, FrameLayout container, BannerVisible callback) {

        String adUnit = adUnits.get("banner");
        if (adUnit == null || adUnit.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnit);

        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(View.GONE);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(View.VISIBLE);

                // ✅ Wait until layout is drawn so we get real height
                container.post(() -> {
                    int heightPx = container.getHeight();  // REAL height
                    if (callback != null) callback.onBannerVisible(heightPx);
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                container.setVisibility(View.GONE);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    public interface BannerVisible {
        void onBannerVisible(int bannerHeightPx);
    }



    // ----------------------------
    // 🟥 Interstitial Ads
    // ----------------------------
    private static void preloadInterstitial(Context context) {
        String adUnit = adUnits.get("inter");
        if (adUnit == null || adUnit.isEmpty()) {
            Log.w(TAG, "⚠️ Interstitial adUnit missing");
            return;
        }

        InterstitialAd.load(context, adUnit, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        Log.d(TAG, "✅ Interstitial preloaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e(TAG, "❌ Interstitial load failed: " + error.getMessage());
                    }
                });
    }

    public static void showInterstitial(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    Log.d(TAG, "ℹ️ Interstitial closed — reloading...");
                    preloadInterstitial(activity);
                }
            });
        } else {
            Log.w(TAG, "⚠️ Interstitial not ready — preloading...");
            preloadInterstitial(activity);
        }
    }

    // ----------------------------
    // 🟩 Rewarded Ads
    // ----------------------------
    private static void preloadRewarded(Context context) {
        String adUnit = adUnits.get("reward");
        if (adUnit == null || adUnit.isEmpty()) {
            Log.w(TAG, "⚠️ Rewarded adUnit missing");
            return;
        }

        RewardedAd.load(context, adUnit, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "✅ Rewarded preloaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e(TAG, "❌ Rewarded load failed: " + error.getMessage());
                    }
                });
    }

    public static void showRewarded(Activity activity, RewardCallback callback) {
        if (rewardedAd != null) {
            rewardedAd.show(activity, rewardItem -> {
                Log.d(TAG, "🎁 Reward earned: " + rewardItem.getAmount());
                callback.onRewardEarned(rewardItem);
            });
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    Log.d(TAG, "ℹ️ Rewarded closed — reloading...");
                    preloadRewarded(activity);
                }
            });
        } else {
            Log.w(TAG, "⚠️ Rewarded not ready — preloading...");
            preloadRewarded(activity);
        }
    }

    // ----------------------------
    // 🟨 Native Ads
    // ----------------------------
//    public static void loadNativeAd(Context context, FrameLayout container) {
//        String adUnit = adUnits.get("native");
//        if (adUnit == null || adUnit.isEmpty()) {
//            Log.w(TAG, "⚠️ Native adUnit missing");
//            container.setVisibility(View.GONE);
//            return;
//        }
//
//        container.setVisibility(View.GONE);
//
//        AdLoader adLoader = new AdLoader.Builder(context, adUnit)
//                .forNativeAd(nativeAd -> {
//                    nativeAdInstance = nativeAd;
//                    Log.d(TAG, "✅ Native Ad Loaded");
//
//                    if (container != null) {
//                        LayoutInflater inflater = LayoutInflater.from(context);
//                        NativeAdView adView = (NativeAdView)
//                        populateNativeAdView(nativeAd, adView);
//                        container.removeAllViews();
//                        container.addView(adView);
//                        container.setVisibility(View.VISIBLE);
//                    }
//                })
//                .withAdListener(new AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
//                        container.setVisibility(View.GONE);
//                        Log.e(TAG, "❌ Native Ad Failed: " + error.getMessage());
//                    }
//                })
//                .build();
//
//        adLoader.loadAd(new AdRequest.Builder().build());
//    }

    private static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        ((TextView) adView.findViewById(R.id.ad_headline)).setText(nativeAd.getHeadline());
        ((TextView) adView.findViewById(R.id.ad_body)).setText(nativeAd.getBody());
        ((Button) adView.findViewById(R.id.ad_call_to_action)).setText(nativeAd.getCallToAction());

        ImageView icon = adView.findViewById(R.id.ad_app_icon);
        if (nativeAd.getIcon() != null)
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());

        adView.setNativeAd(nativeAd);
    }

    // ----------------------------
    // 🎁 Reward Callback Interface
    // ----------------------------
    public interface RewardCallback {
        void onRewardEarned(RewardItem reward);
    }

    /**
     * -----------------------------------------------------------
     * ✅ Get Extra Firebase Config Values (non-ad keys)
     * -----------------------------------------------------------
     * Returns a value for any custom key received from Firebase.
     * Example usage: AdsManager.getConfigValue("trans_cnt")
     */

    public static int getTransactionAdFrequency() {
        try {
            String count = adUnits.get("trans_cnt");
            if (count == null) return 0;
            return Integer.parseInt(count.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getAccDetailsBannerAdFrequency() {
        try {
            String count = adUnits.get("acc_details_banner_cnt");
            if (count == null) return 0;
            return Integer.parseInt(count.trim());
        } catch (Exception e) {
            return 0;
        }
    }


}
