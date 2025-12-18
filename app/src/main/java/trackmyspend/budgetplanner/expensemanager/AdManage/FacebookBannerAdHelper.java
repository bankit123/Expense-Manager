package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.ads.*;

public class FacebookBannerAdHelper {

    private static final String TAG = "FacebookBannerAd";

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    // ================= NEW METHOD (ADDED) =================
    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            String bannerSize,
                            Callback callback) {

        if (activity == null || container == null) {
            Log.w(TAG, "Activity or container is NULL → abort");
            if (callback != null) callback.onFailed();
            return;
        }

        if (config == null) {
            Log.w(TAG, "AdsConfig is NULL → abort");
            if (callback != null) callback.onFailed();
            return;
        }

        String fbUnit = config.get("fb_banner");
        Log.d(TAG, "FB Banner Unit = " + fbUnit);

        if (fbUnit == null || fbUnit.isEmpty()) {
            Log.w(TAG, "FB Banner unit ID missing");
            if (callback != null) callback.onFailed();
            return;
        }

        // ✅ banner size comes from CALLER (if provided)
        boolean isLarge = "l".equalsIgnoreCase(bannerSize);
        Log.d(TAG, "Banner size = " + (isLarge ? "LARGE" : "SMALL"));

        AdSize fbSize = isLarge
                ? AdSize.RECTANGLE_HEIGHT_250
                : AdSize.BANNER_HEIGHT_50;

        Log.d(TAG, "Creating Facebook AdView with size = " + fbSize);

        AdView fbAdView = new AdView(activity, fbUnit, fbSize);

        // ⚠️ Test device (remove in production)
//        AdSettings.addTestDevice("1cfdec71-440b-467a-956b-40fe5ea023ec");
        Log.d(TAG, "Test device added");

        container.removeAllViews();
        container.addView(fbAdView);
        container.setVisibility(View.GONE);

        Log.d(TAG, "Loading Facebook banner ad...");

        fbAdView.loadAd(
                fbAdView.buildLoadAdConfig()
                        .withAdListener(new AdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {
                                Log.d(TAG, "Facebook banner LOADED");
                                container.setVisibility(View.VISIBLE);
                                if (callback != null) callback.onLoaded();
                            }

                            @Override
                            public void onError(Ad ad, AdError error) {
                                Log.e(TAG, "Facebook banner FAILED → "
                                        + error.getErrorCode() + " | "
                                        + error.getErrorMessage());
                                container.setVisibility(View.GONE);
                                if (callback != null) callback.onFailed();
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.d(TAG, "Facebook banner CLICKED");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                Log.d(TAG, "Facebook banner IMPRESSION logged");
                            }
                        })
                        .build()
        );
    }
}
