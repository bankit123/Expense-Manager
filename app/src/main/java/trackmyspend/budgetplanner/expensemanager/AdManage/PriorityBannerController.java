package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.widget.FrameLayout;

public class PriorityBannerController {

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config) {

        String priority = config.get("priority_banner_ads"); // "fb" or "google"

        if ("fb".equalsIgnoreCase(priority)) {
            showFacebookFirst(activity, container, config);
        } else {
            showGoogleFirst(activity, container, config);
        }
    }

    // ---------------- GOOGLE → FB ----------------

    private static void showGoogleFirst(Activity activity,
                                        FrameLayout container,
                                        AdsConfig config) {

        GoogleBannerAdHelper.show(activity, container, config,
                new GoogleBannerAdHelper.Callback() {
                    @Override
                    public void onLoaded() {}

                    @Override
                    public void onFailed() {
                        showFacebookFallback(activity, container, config);
                    }
                });
    }

    // ---------------- FB → GOOGLE ----------------

    private static void showFacebookFirst(Activity activity,
                                          FrameLayout container,
                                          AdsConfig config) {

        FacebookBannerAdHelper.show(activity, container, config,
                new FacebookBannerAdHelper.Callback() {
                    @Override
                    public void onLoaded() {}

                    @Override
                    public void onFailed() {
                        showGoogleFallback(activity, container, config);
                    }
                });
    }

    // ---------------- FALLBACKS ----------------

    private static void showGoogleFallback(Activity activity,
                                           FrameLayout container,
                                           AdsConfig config) {

        GoogleBannerAdHelper.show(activity, container, config, null);
    }

    private static void showFacebookFallback(Activity activity,
                                             FrameLayout container,
                                             AdsConfig config) {

        FacebookBannerAdHelper.show(activity, container, config, null);
    }
}

