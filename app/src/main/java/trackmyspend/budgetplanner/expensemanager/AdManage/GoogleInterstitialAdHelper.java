package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class GoogleInterstitialAdHelper {

    private static InterstitialAd googleInterstitialAd;

    public interface Callback {
        void onShown();
        void onDismissed();
        void onFailed();
        void onNotReady();
    }

    // ---------------- PRELOAD ----------------

    public static void preload(Context context, AdsConfig config) {

        String adUnit = config.get("inter");
        if (adUnit == null || adUnit.isEmpty()) return;

        InterstitialAd.load(
                context,
                adUnit,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        googleInterstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError error) {
                        googleInterstitialAd = null;
                    }
                }
        );
    }

    // ---------------- SHOW ----------------

    public static void show(Activity activity, Callback callback) {

        if (googleInterstitialAd == null) {
            if (callback != null) callback.onNotReady();
            return;
        }

        googleInterstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {

                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (callback != null) callback.onShown();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        googleInterstitialAd = null;
                        if (callback != null) callback.onDismissed();

                        // 🔄 Preload next ad
                        preload(activity, AdsManager.getConfig());
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        googleInterstitialAd = null;
                        if (callback != null) callback.onFailed();

                        // 🔄 Try loading next
                        preload(activity, AdsManager.getConfig());
                    }
                }
        );

        googleInterstitialAd.show(activity);
    }
}
