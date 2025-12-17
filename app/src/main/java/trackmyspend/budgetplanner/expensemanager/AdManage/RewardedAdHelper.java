package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.*;

public class RewardedAdHelper {

    private static RewardedAd rewardedAd;

    public interface Callback {
        void onRewardEarned(RewardItem reward);
        void onClosed();
        void onFailed();
        void onNotReady();
    }

    public static void preload(Context context, AdsConfig config) {
        String adUnit = config.get("reward");
        if (adUnit == null) return;

        RewardedAd.load(
                context,
                adUnit,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }
                }
        );
    }

    public static void show(Activity activity, Callback callback) {

        if (rewardedAd == null) {
            if (callback != null) callback.onNotReady();
            return;
        }

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        if (callback != null) callback.onClosed();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        rewardedAd = null;
                        if (callback != null) callback.onFailed();
                    }
                }
        );

        rewardedAd.show(activity, reward -> {
            if (callback != null) callback.onRewardEarned(reward);
        });
    }
}
