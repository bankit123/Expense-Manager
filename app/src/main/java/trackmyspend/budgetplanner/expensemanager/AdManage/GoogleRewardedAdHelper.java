package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class GoogleRewardedAdHelper {

    private static RewardedAd rewardedAd;

    public interface Callback {
        void onShown();
        void onRewardEarned();
        void onDismissed();
        void onFailed();
        void onNotReady();
    }

    // ---------------- PRELOAD ----------------

    public static void preload(Context context, AdsConfig config) {
        String adUnit = config.get("reward");
        if (adUnit == null || adUnit.isEmpty()) return;

        RewardedAd.load(
                context,
                adUnit,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        rewardedAd = null;
                    }
                }
        );
    }

    // ---------------- SHOW ----------------

    public static void show(Activity activity, Callback callback) {

        if (rewardedAd == null) {
            if (callback != null) callback.onNotReady();
            return;
        }

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {

                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (callback != null) callback.onShown();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        if (callback != null) callback.onDismissed();
                        preload(activity, AdsManager.getConfig());
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        rewardedAd = null;
                        if (callback != null) callback.onFailed();
                        preload(activity, AdsManager.getConfig());
                    }
                }
        );

        rewardedAd.show(activity, rewardItem -> {
            if (callback != null) callback.onRewardEarned();
        });
    }
}
