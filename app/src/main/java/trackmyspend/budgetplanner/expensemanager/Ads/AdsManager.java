package trackmyspend.budgetplanner.expensemanager.Ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
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
 * AdsManager — AppOpen-ready version
 * - reads new Firebase nested structure (test_ads/ads_id and /ads_controller)
 * - normalizes app_open -> appOpen
 * - preloads AppOpen and exposes showAppOpen(activity, onComplete)
 */
public class AdsManager {

    private static final String TAG = "SmartAdsManager";

    private static boolean isInitialized = false;
    private static final Map<String, String> adUnits = new HashMap<>();

    // Fallback Test Ad IDs (Google Official Test IDs). appOpen set to your provided test id.
    private static final Map<String, String> TEST_ADS = Map.of(
            "appID", "ca-app-pub-3940256099942544~3347511713",
            "banner", "ca-app-pub-3940256099942544/92145897411",
            "inter", "ca-app-pub-3940256099942544/1033173712",
            "reward", "ca-app-pub-3940256099942544/5224354917",
            "native", "ca-app-pub-3940256099942544/2247696110",
            "appOpen", "ca-app-pub-3940256099942544/9257395921"
    );

    private static InterstitialAd interstitialAd;
    private static RewardedAd rewardedAd;
    private static NativeAd nativeAdInstance;

    // --- App Open ad fields ---
//    private static AppOpenAd appOpenAd;
//    private static long appOpenLoadTime = 0L;
//    private static boolean isShowingAppOpen = false;
    // refresh threshold (4 hours recommended by Google)
//    private static final long APP_OPEN_EXPIRY_MILLIS = 4 * 60 * 60 * 1000L;

    // Interface for splash callback
    public interface InitCallback {
        void onInitialized();
    }

    // ----------------------------
    // Initialization
    // ----------------------------
    public static void initialize(Context context, InitCallback callback) {
        if (isInitialized) {
            Log.d(TAG, "ℹ️ AdsManager already initialized");
            if (callback != null) callback.onInitialized();
            return;
        }

        Log.d(TAG, "🚀 Initializing SmartAdsManager...");
        isInitialized = true;

        // Safety timeout — fallback to test ads if Firebase is too slow
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (adUnits.isEmpty()) {
                Log.w(TAG, "⚠️ Firebase took too long — using default test ads");
                adUnits.putAll(TEST_ADS);
                initAdMob(context, TEST_ADS.get("appID"));
                if (callback != null) callback.onInitialized();
            }
        }, 5000); // 5 seconds timeout

        fetchAdIdsFromFirebase(context, callback);
    }

    // Fetch AdMob IDs from Firebase (new nested structure only)
    private static void fetchAdIdsFromFirebase(Context context, InitCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("test_ads");

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                adUnits.clear();
                DataSnapshot root = task.getResult();

                // Expecting new nested structure: test_ads -> ads_id and ads_controller
                DataSnapshot adsIdSnap = root.child("ads_id");
                DataSnapshot adsCtrlSnap = root.child("ads_controller");

                if (adsIdSnap.exists() || adsCtrlSnap.exists()) {

                    // Read ads_id children (appID, banner, inter, reward, native, app_open, ...)
                    if (adsIdSnap.exists()) {
                        for (DataSnapshot child : adsIdSnap.getChildren()) {
                            String key = child.getKey();
                            Object val = child.getValue();
                            if (key != null && val != null) {
                                // normalize snake_case app_open -> appOpen

                                    adUnits.put(key, String.valueOf(val));

                            }
                        }
                        Log.d(TAG, "DEBUG: ads_id.appOpen -> " + adUnits.get("appOpen"));
                    }

                    // Read ads_controller children and map known controller keys to internal keys used by getters
                    if (adsCtrlSnap.exists()) {
                        Object accCntVal = adsCtrlSnap.child("b_acc_details_ads_cnt").getValue();
                        if (accCntVal != null) {
                            adUnits.put("acc_details_banner_cnt", String.valueOf(accCntVal));
                        }

                        Object transCntVal = adsCtrlSnap.child("b_home_trans_ads_cnt").getValue();
                        if (transCntVal != null) {
                            adUnits.put("trans_cnt", String.valueOf(transCntVal));
                        }

                        // Also store any other controller keys under their original names (stringified)
                        for (DataSnapshot child : adsCtrlSnap.getChildren()) {
                            String key = child.getKey();
                            Object val = child.getValue();
                            if (key != null && val != null) {
                                if ("b_acc_details_ads_cnt".equals(key) || "b_home_trans_ads_cnt".equals(key)) continue;
                                adUnits.put(key, String.valueOf(val));
                            }
                        }
                    }
                } else {
                    // NEW STRUCTURE MISSING — fallback to TEST_ADS
                    Log.e(TAG, "❌ Expected new nested structure (ads_id / ads_controller) not present in Firebase.");
                    adUnits.putAll(TEST_ADS);
                }

                Log.d(TAG, "✅ Firebase ad config loaded (new structure only): " + adUnits);

                // Try to find appId from ads_id.appID
                String appId = adUnits.get("appID");
                if (appId != null && !appId.trim().isEmpty()) {
                    initAdMob(context, appId);
                } else {
                    Log.w(TAG, "⚠️ Missing 'appID' in new structure — using test ads");
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

    private static void initAdMob(Context context, String appId) {
        try {
            Log.d(TAG, "🧩 Initializing AdMob with App ID: " + appId);

            MobileAds.initialize(context, initializationStatus -> {
                logInitializationStatus(initializationStatus);
                preloadInterstitial(context);
                preloadRewarded(context);
                // preload app open too
//                preloadAppOpen(context);
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
    // Banner / Interstitial / Rewarded (kept similar)
    // ----------------------------

    public static void loadBanner(Activity activity, android.widget.FrameLayout container) {
        String adUnit = adUnits.get("banner");
        if (adUnit == null || adUnit.isEmpty()) {
            Log.w(TAG, "⚠️ Banner adUnit missing");
            container.setVisibility(android.view.View.GONE);
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnit);

        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(android.view.View.GONE);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(android.view.View.VISIBLE);
                Log.d(TAG, "✅ Banner Ad Loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                container.setVisibility(android.view.View.GONE);
                Log.e(TAG, "❌ Banner failed: " + adError.getMessage());
            }
        });

        adView.loadAd(adRequest);
    }

    public static void loadBanner(Activity activity, android.widget.FrameLayout container, BannerVisible callback) {
        String adUnit = adUnits.get("banner");
        if (adUnit == null || adUnit.isEmpty()) {
            container.setVisibility(android.view.View.GONE);
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnit);

        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(android.view.View.GONE);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(android.view.View.VISIBLE);
                container.post(() -> {
                    int heightPx = container.getHeight();
                    if (callback != null) callback.onBannerVisible(heightPx);
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                container.setVisibility(android.view.View.GONE);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    public interface BannerVisible {
        void onBannerVisible(int bannerHeightPx);
    }

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

    // inside AdsManager class (add near existing interstitial methods)

    // Callback interface for interstitial dismiss
    public interface InterstitialCallback {
        void onAdDismissed();
    }

    /**
     * Backwards-compatible simple call (keeps existing behavior)
     */
    public static void showInterstitial(Activity activity) {
        showInterstitial(activity, null);
    }

    /**
     * New overload: show interstitial and call callback when ad dismissed (or immediately if no ad).
     * Use this when you need to grant points after the user finishes watching the interstitial.
     */
    public static void showInterstitial(Activity activity, @Nullable InterstitialCallback callback) {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    Log.d(TAG, "ℹ️ Interstitial closed — reloading...");
                    preloadInterstitial(activity);
                    if (callback != null) {
                        try { callback.onAdDismissed(); } catch (Exception e) { Log.e(TAG, "callback error: " + e.getMessage()); }
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(TAG, "❌ Interstitial failed to show: " + adError.getMessage());
                    interstitialAd = null;
                    preloadInterstitial(activity);
                    if (callback != null) {
                        try { callback.onAdDismissed(); } catch (Exception e) { Log.e(TAG, "callback error: " + e.getMessage()); }
                    }
                }
            });
        } else {
            Log.w(TAG, "⚠️ Interstitial not ready — preloading...");
            preloadInterstitial(activity);
            // Option: call callback immediately so UI can continue (you decide; here we call back)
            if (callback != null) {
                try { callback.onAdDismissed(); } catch (Exception e) { Log.e(TAG, "callback error: " + e.getMessage()); }
            }
        }
    }


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

    private static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        ((android.widget.TextView) adView.findViewById(R.id.ad_headline)).setText(nativeAd.getHeadline());
        ((android.widget.TextView) adView.findViewById(R.id.ad_body)).setText(nativeAd.getBody());
        ((android.widget.Button) adView.findViewById(R.id.ad_call_to_action)).setText(nativeAd.getCallToAction());

        ImageView icon = adView.findViewById(R.id.ad_app_icon);
        if (nativeAd.getIcon() != null)
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());

        adView.setNativeAd(nativeAd);
    }

    // ----------------------------
    // App Open Ads
    // ----------------------------
//    public static void preloadAppOpen(Context context) {
//        String adUnit = adUnits.get("appOpen");
//        if (adUnit == null || adUnit.isEmpty()) {
//            Log.w(TAG, "⚠️ App Open adUnit missing - using test fallback");
//            adUnit = TEST_ADS.get("appOpen");
//        }
//
//        AdRequest request = new AdRequest.Builder().build();
//
//        AppOpenAd.load(context, adUnit, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                new AppOpenAd.AppOpenAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull AppOpenAd ad) {
//                        appOpenAd = ad;
//                        appOpenLoadTime = System.currentTimeMillis();
//                        Log.d(TAG, "✅ App Open ad loaded — time=" + appOpenLoadTime + " id=" + adUnits.get("appOpen"));
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        appOpenAd = null;
//                        appOpenLoadTime = 0;
//                        Log.e(TAG, "❌ App Open ad failed to load for id=" + adUnits.get("appOpen") + " : " + loadAdError.getMessage());
//                    }
//                });
//    }

//    private static boolean isAppOpenAdAvailable() {
//        return appOpenAd != null && (System.currentTimeMillis() - appOpenLoadTime) < APP_OPEN_EXPIRY_MILLIS;
//    }

    /**
     * Show App Open and run onComplete after ad dismissed or immediately if no ad available.
     */
//    public static void showAppOpen(Activity activity, @Nullable Runnable onComplete) {
//        if (isShowingAppOpen) {
//            Log.d(TAG, "ℹ️ App Open already showing");
//            if (onComplete != null) onComplete.run();
//            return;
//        }
//
//        Log.d(TAG, "DEBUG showAppOpen(): isAvailable=" + isAppOpenAdAvailable() + " appOpenId=" + adUnits.get("appOpen"));
//
//        if (isAppOpenAdAvailable()) {
//            isShowingAppOpen = true;
//            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    Log.d(TAG, "ℹ️ App Open dismissed — clearing and preloading new one");
//                    appOpenAd = null;
//                    isShowingAppOpen = false;
//                    preloadAppOpen(activity);
//                    if (onComplete != null) {
//                        try { onComplete.run(); } catch (Exception e) { Log.e(TAG, "onComplete threw: " + e.getMessage()); }
//                    }
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
//                    Log.e(TAG, "❌ App Open failed to show: " + adError.getMessage());
//                    appOpenAd = null;
//                    isShowingAppOpen = false;
//                    preloadAppOpen(activity);
//                    if (onComplete != null) {
//                        try { onComplete.run(); } catch (Exception e) { Log.e(TAG, "onComplete threw: " + e.getMessage()); }
//                    }
//                }
//
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    Log.d(TAG, "✅ App Open shown");
//                }
//            });
//
//            try {
//                appOpenAd.show(activity);
//            } catch (Exception e) {
//                Log.e(TAG, "❌ Exception while showing App Open: " + e.getMessage());
//                isShowingAppOpen = false;
//                appOpenAd = null;
//                preloadAppOpen(activity);
//                if (onComplete != null) onComplete.run();
//            }
//        } else {
//            Log.w(TAG, "⚠️ No App Open available — preloading now and continuing");
//            preloadAppOpen(activity);
//            if (onComplete != null) onComplete.run();
//        }
//    }

    // Backwards-compatible simple call
//    public static void showAppOpen(Activity activity) {
//        showAppOpen(activity, null);
//    }

    // Helper: query readiness
//    public static boolean isAppOpenReady() {
//        return isAppOpenAdAvailable();
//    }

    // ----------------------------
    // Reward callback interface
    // ----------------------------
    public interface RewardCallback {
        void onRewardEarned(RewardItem reward);
    }

    // ----------------------------
    // Extra getters for ad frequency from adUnits
    // ----------------------------
    public static int getTransactionAdFrequency() {
        try {
            String count = adUnits.get("trans_cnt"); // mapped from b_home_trans_ads_cnt
            if (count == null) return 0;
            return Integer.parseInt(count.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getAccDetailsBannerAdFrequency() {
        try {
            String count = adUnits.get("acc_details_banner_cnt"); // mapped from b_acc_details_ads_cnt
            if (count == null) return 0;
            return Integer.parseInt(count.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
