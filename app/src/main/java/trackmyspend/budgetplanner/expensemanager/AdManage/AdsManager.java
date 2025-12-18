package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;

public class AdsManager {

    private static final String TAG = "AdsManager";

    private static AdsConfig adsConfig;
    private static boolean initialized = false;

    public interface InitCallback {
        void onReady();
        void onFailed();
    }

    public static void initialize(Context context, InitCallback callback) {

        AdsConfigRepository.fetch(new AdsConfigRepository.Callback() {

            @Override
            public void onSuccess(AdsConfig config) {
                adsConfig = config;

                if (!initialized) {
                    initialized = true;

                    // AdMob
                    MobileAds.initialize(context, status ->
                            Log.d(TAG, "AdMob initialized")
                    );

                    // Facebook Audience Network
                    AudienceNetworkAds.initialize(context);
                    Log.d(TAG, "Facebook Audience Network initialized");
                }

                // Preload (banner loads on demand)
                GoogleInterstitialAdHelper.preload(context, adsConfig);
                FacebookInterstitialHelper.preload((Activity) context, adsConfig);

                GoogleRewardedAdHelper.preload(context, adsConfig);
                FacebookRewardedAdHelper.preload((Activity) context, adsConfig);

                GoogleNativeAdHelper.preload((Activity) context, AdsManager.getConfig());
                FacebookNativeAdHelper.preload((Activity) context, AdsManager.getConfig());


                RewardedAdHelper.preload(context, adsConfig);

                if (callback != null) callback.onReady();
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Firebase ads config failed");
                if (callback != null) callback.onFailed();
            }
        });
    }

    public static AdsConfig getConfig() {
        return adsConfig;
    }
}
