package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

public class PriorityBannerController {

    private static final String TAG = "PriorityBannerCtrl";

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            String bannerSize) {

        if (config == null) {
            Log.w(TAG, "AdsConfig is null → Banner not shown");
            return;
        }

        Log.d(TAG, "Banner size from caller = " + bannerSize);

        String priority = config.get("priority_banner_ads"); // fb / google
        Log.d(TAG, "Banner priority = " + priority);

        if ("fb".equalsIgnoreCase(priority)) {
            showFacebookFirst(activity, container, config, bannerSize);
        } else {
            showGoogleFirst(activity, container, config, bannerSize);
        }
    }

    // ---------------- GOOGLE → FB ----------------

    private static void showGoogleFirst(Activity activity,
                                        FrameLayout container,
                                        AdsConfig config,
                                        String bannerSize) {

        Log.d(TAG, "Trying Google banner...");

        GoogleBannerAdHelper.show(activity, container, config, bannerSize,
                new GoogleBannerAdHelper.Callback() {
                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "Google banner LOADED");
                    }

                    @Override
                    public void onFailed() {
                        Log.w(TAG, "Google banner FAILED → FB fallback");
                        showFacebookFallback(activity, container, config, bannerSize);
                    }
                });
    }

    // ---------------- FB → GOOGLE ----------------

    private static void showFacebookFirst(Activity activity,
                                          FrameLayout container,
                                          AdsConfig config,
                                          String bannerSize) {

        Log.d(TAG, "Trying Facebook banner...");

        FacebookBannerAdHelper.show(activity, container, config, bannerSize,
                new FacebookBannerAdHelper.Callback() {
                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "Facebook banner LOADED");
                    }

                    @Override
                    public void onFailed() {
                        Log.w(TAG, "Facebook banner FAILED → Google fallback");
                        showGoogleFallback(activity, container, config, bannerSize);
                    }
                });
    }

    // ---------------- FALLBACKS ----------------

    private static void showGoogleFallback(Activity activity,
                                           FrameLayout container,
                                           AdsConfig config,
                                           String bannerSize) {

        GoogleBannerAdHelper.show(activity, container, config, bannerSize, null);
    }

    private static void showFacebookFallback(Activity activity,
                                             FrameLayout container,
                                             AdsConfig config,
                                             String bannerSize) {

        FacebookBannerAdHelper.show(activity, container, config, bannerSize, null);
    }
}
