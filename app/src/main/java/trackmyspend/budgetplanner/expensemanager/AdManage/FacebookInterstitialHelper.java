package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;

import com.facebook.ads.*;

public class FacebookInterstitialHelper {

    private static InterstitialAd fbInterstitial;

    public interface Callback {
        void onShown();
        void onDismissed();
        void onFailed();
        void onNotReady();
    }

    public static void preload(Activity activity, AdsConfig config) {

        String fbUnit = config.get("fb_inter");
        if (fbUnit == null || fbUnit.isEmpty()) return;

        fbInterstitial = new InterstitialAd(activity, fbUnit);

        fbInterstitial.loadAd(
                fbInterstitial.buildLoadAdConfig()
                        .withAdListener(new InterstitialAdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {}

                            @Override
                            public void onError(Ad ad, AdError error) {
                                fbInterstitial = null;
                            }

                            @Override
                            public void onInterstitialDisplayed(Ad ad) {}

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                fbInterstitial = null;
                                // ✅ Notify dismiss
                                if (pendingCallback != null) {
                                    pendingCallback.onDismissed();
                                    pendingCallback = null;
                                }
                            }

                            @Override public void onAdClicked(Ad ad) {}
                            @Override public void onLoggingImpression(Ad ad) {}
                        })
                        .build()
        );
    }

    private static Callback pendingCallback;

    public static void show(Activity activity, Callback callback) {

        if (fbInterstitial == null || !fbInterstitial.isAdLoaded()) {
            if (callback != null) callback.onNotReady();
            return;
        }

        pendingCallback = callback;
        fbInterstitial.show();

        if (callback != null) callback.onShown();
    }
}
