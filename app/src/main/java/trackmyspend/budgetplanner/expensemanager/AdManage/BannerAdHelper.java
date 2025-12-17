package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

public class BannerAdHelper {

    private static final String TAG = "BannerAdHelper";

    public interface Callback {
        void onAdMobLoaded();
        void onFacebookLoaded();
        void onAllFailed();
    }

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            Callback callback) {

        container.removeAllViews();
        container.setVisibility(View.GONE);

        // 🔹 Read banner size from Firebase
        String bannerSize = config.get("banner_size"); // "L" or "S"
        boolean isLarge = "L".equalsIgnoreCase(bannerSize);

        String admobUnit = config.get("banner");

        if (admobUnit == null || admobUnit.isEmpty()) {
            loadFacebookBanner(activity, container, config, isLarge, callback);
            return;
        }

        // -------------------- ADMOB --------------------
        com.google.android.gms.ads.AdView admobView =
                new com.google.android.gms.ads.AdView(activity);

        admobView.setAdUnitId(admobUnit);

        // ✅ Dynamic AdMob size
        admobView.setAdSize(
                isLarge
                        ? com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE
                        : com.google.android.gms.ads.AdSize.BANNER
        );

        container.addView(admobView);

        admobView.setAdListener(new com.google.android.gms.ads.AdListener() {

            @Override
            public void onAdLoaded() {
                container.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ AdMob banner loaded (" + (isLarge ? "L" : "S") + ")");
                if (callback != null) callback.onAdMobLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                Log.w(TAG, "❌ AdMob failed → fallback to Facebook");
                container.removeAllViews();
                loadFacebookBanner(activity, container, config, isLarge, callback);
            }
        });

        admobView.loadAd(new AdRequest.Builder().build());
    }

    // --------------------------------------------------
    // FACEBOOK FALLBACK
    // --------------------------------------------------

    private static void loadFacebookBanner(Activity activity,
                                           FrameLayout container,
                                           AdsConfig config,
                                           boolean isLarge,
                                           Callback callback) {

        String fbUnit = config.get("fb_banner");

        if (fbUnit == null || fbUnit.isEmpty()) {
            Log.e(TAG, "❌ Facebook banner id missing");
            if (callback != null) callback.onAllFailed();
            return;
        }

        // 🔥 Test device (DEBUG ONLY recommended)
        AdSettings.addTestDevice("1cfdec71-440b-467a-956b-40fe5ea023ec");

        // ✅ Dynamic Facebook size
        AdSize fbSize = isLarge
                ? AdSize.RECTANGLE_HEIGHT_250
                : AdSize.BANNER_HEIGHT_50;

        com.facebook.ads.AdView fbAdView =
                new com.facebook.ads.AdView(activity, fbUnit, fbSize);

        container.addView(fbAdView);

        fbAdView.loadAd(
                fbAdView.buildLoadAdConfig()
                        .withAdListener(new com.facebook.ads.AdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {
                                container.setVisibility(View.VISIBLE);
                                Log.d(TAG, "✅ Facebook banner loaded (" + (isLarge ? "L" : "S") + ")");
                                if (callback != null) callback.onFacebookLoaded();
                            }

                            @Override
                            public void onError(Ad ad, AdError error) {
                                container.setVisibility(View.GONE);
                                Log.e(TAG, "❌ Facebook banner failed: " + error.getErrorMessage());
                                if (callback != null) callback.onAllFailed();
                            }

                            @Override public void onAdClicked(Ad ad) {}
                            @Override public void onLoggingImpression(Ad ad) {}
                        })
                        .build()
        );
    }
}
