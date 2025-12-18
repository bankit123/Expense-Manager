package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;

public class PriorityRewardedController {

    public static void show(Activity activity,
                            AdsConfig config,
                            GoogleRewardedAdHelper.Callback callback) {

        String priority = config.get("priority_reward_ads"); // "google" or "fb"

        if ("fb".equalsIgnoreCase(priority)) {
            showFacebookFirst(activity, config, callback);
        } else {
            showGoogleFirst(activity, config, callback);
        }
    }

    // ---------------- GOOGLE → FACEBOOK ----------------

    private static void showGoogleFirst(Activity activity,
                                        AdsConfig config,
                                        GoogleRewardedAdHelper.Callback callback) {

        GoogleRewardedAdHelper.show(activity, new GoogleRewardedAdHelper.Callback() {

            @Override public void onShown() {
                callback.onShown();
            }

            @Override public void onRewardEarned() {
                callback.onRewardEarned();
            }

            @Override public void onDismissed() {
                callback.onDismissed();
                preloadNext(activity, config);
            }

            @Override public void onFailed() {
                showFacebookFallback(activity, config, callback);
            }

            @Override public void onNotReady() {
                showFacebookFallback(activity, config, callback);
            }
        });
    }

    // ---------------- FACEBOOK → GOOGLE ----------------

    private static void showFacebookFirst(Activity activity,
                                          AdsConfig config,
                                          GoogleRewardedAdHelper.Callback callback) {

        FacebookRewardedAdHelper.show(activity, new FacebookRewardedAdHelper.Callback() {

            @Override public void onShown() {
                callback.onShown();
            }

            @Override public void onRewardEarned() {
                callback.onRewardEarned();
            }

            @Override public void onDismissed() {
                callback.onDismissed();
                preloadNext(activity, config);
            }

            @Override public void onFailed() {
                showGoogleFallback(activity, config, callback);
            }

            @Override public void onNotReady() {
                showGoogleFallback(activity, config, callback);
            }
        });
    }

    // ---------------- FALLBACKS ----------------

    private static void showGoogleFallback(Activity activity,
                                           AdsConfig config,
                                           GoogleRewardedAdHelper.Callback callback) {

        GoogleRewardedAdHelper.show(activity, new GoogleRewardedAdHelper.Callback() {

            @Override public void onShown() {
                callback.onShown();
            }

            @Override public void onRewardEarned() {
                callback.onRewardEarned();
            }

            @Override public void onDismissed() {
                callback.onDismissed();
                preloadNext(activity, config);
            }

            @Override public void onFailed() {
                callback.onFailed();
            }

            @Override public void onNotReady() {
                callback.onFailed();
            }
        });
    }

    private static void showFacebookFallback(Activity activity,
                                             AdsConfig config,
                                             GoogleRewardedAdHelper.Callback callback) {

        FacebookRewardedAdHelper.show(activity, new FacebookRewardedAdHelper.Callback() {

            @Override public void onShown() {
                callback.onShown();
            }

            @Override public void onRewardEarned() {
                callback.onRewardEarned();
            }

            @Override public void onDismissed() {
                callback.onDismissed();
                preloadNext(activity, config);
            }

            @Override public void onFailed() {
                callback.onFailed();
            }

            @Override public void onNotReady() {
                callback.onFailed();
            }
        });
    }

    // ---------------- PRELOAD NEXT ----------------

    private static void preloadNext(Activity activity, AdsConfig config) {
        GoogleRewardedAdHelper.preload(activity, config);
        FacebookRewardedAdHelper.preload(activity, config);
    }
}
