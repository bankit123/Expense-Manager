package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;

import com.facebook.ads.*;

public class FacebookRewardedAdHelper {

    private static RewardedVideoAd fbRewardedAd;
    private static Callback pendingCallback;

    public interface Callback {
        void onShown();
        void onRewardEarned();
        void onDismissed();
        void onFailed();
        void onNotReady();
    }

    // ---------------- PRELOAD ----------------

    public static void preload(Activity activity, AdsConfig config) {

        String fbUnit = config.get("fb_reward");
        if (fbUnit == null || fbUnit.isEmpty()) return;

        fbRewardedAd = new RewardedVideoAd(activity, fbUnit);

        fbRewardedAd.loadAd(
                fbRewardedAd.buildLoadAdConfig()
                        .withAdListener(new RewardedVideoAdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {}

                            @Override
                            public void onError(Ad ad, AdError error) {
                                fbRewardedAd = null;
                            }

                            @Override
                            public void onRewardedVideoCompleted() {
                                if (pendingCallback != null) {
                                    pendingCallback.onRewardEarned();
                                }
                            }

                            @Override
                            public void onRewardedVideoClosed() {
                                fbRewardedAd = null;
                                if (pendingCallback != null) {
                                    pendingCallback.onDismissed();
                                    pendingCallback = null;
                                }
                            }

                            public void onRewardedVideoOpened() {}
                            @Override public void onLoggingImpression(Ad ad) {}
                            @Override public void onAdClicked(Ad ad) {}
                        })
                        .build()
        );
    }

    // ---------------- SHOW ----------------

    public static void show(Activity activity, Callback callback) {

        if (fbRewardedAd == null || !fbRewardedAd.isAdLoaded()) {
            if (callback != null) callback.onNotReady();
            return;
        }

        pendingCallback = callback;
        fbRewardedAd.show();

        if (callback != null) callback.onShown();
    }
}

