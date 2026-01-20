//package trackmyspend.budgetplanner.expensemanager.AdManage;
//
//import static com.facebook.FacebookSdk.getApplicationContext;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//
//import com.facebook.FacebookSdk;
//import com.facebook.ads.AudienceNetworkAds;
//import com.google.android.gms.ads.MobileAds;
//
//public class AdsManager {
//
//    private static final String TAG = "AdsManager";
//
//    private static AdsConfig adsConfig;
//    private static boolean initialized = false;
//
//    public interface InitCallback {
//        void onReady();
//        void onFailed();
//    }
//
//    public static void initialize(Context context, InitCallback callback) {
//
//        AdsConfigRepository.fetch(new AdsConfigRepository.Callback() {
//
//            @Override
//            public void onSuccess(AdsConfig config) {
//                adsConfig = config;
//
//                if (!initialized) {
//                    initialized = true;
//
//                    // AdMob
//                    MobileAds.initialize(context, status ->
//                            Log.d(TAG, "AdMob initialized")
//                    );
//
//                    String fbAppId =
//                            trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager
//                                    .getConfig()
//                                    .get("f_appId");
//
//                    // Facebook Audience Network
//                    FacebookSdk.setApplicationId(fbAppId);
//                    FacebookSdk.sdkInitialize(getApplicationContext());
//
//                    AudienceNetworkAds.initialize(context);
//                    Log.d(TAG, "Facebook Audience Network initialized");
//                }
//
//                // Preload (banner loads on demand)
//                GoogleNativeAdHelper.preload((Activity) context, AdsManager.getConfig());
//                FacebookNativeAdHelper.preload((Activity) context, AdsManager.getConfig());
//
//                GoogleInterstitialAdHelper.preload(context, adsConfig);
//                FacebookInterstitialHelper.preload((Activity) context, adsConfig);
//
//                GoogleRewardedAdHelper.preload(context, adsConfig);
//                FacebookRewardedAdHelper.preload((Activity) context, adsConfig);
//
//                RewardedAdHelper.preload(context, adsConfig);
//
//                if (callback != null) callback.onReady();
//            }
//
//            @Override
//            public void onFailure() {
//                Log.e(TAG, "Firebase ads config failed");
//                if (callback != null) callback.onFailed();
//            }
//        });
//    }
//
//    public static AdsConfig getConfig() {
//        return adsConfig;
//    }
//}

package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.ads.AdSettings;
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

                    // 🔵 AdMob
                    MobileAds.initialize(context, status ->
                            Log.d(TAG, "AdMob initialized")
                    );

                    // 🔵 Facebook App ID from Firebase
                    String fbAppId = adsConfig.get("f_appId");

                    if (fbAppId != null && !fbAppId.isEmpty()) {

                        Log.d(TAG, "FB AppId from Firebase = " + fbAppId);
//                        AdSettings.setTestMode(true);
                        FacebookSdk.setApplicationId(fbAppId);
                        FacebookSdk.sdkInitialize(context.getApplicationContext());

                        AudienceNetworkAds.initialize(context.getApplicationContext());

                        Log.d(TAG, "Facebook Audience Network initialized");

                    } else {
                        Log.e(TAG, "❌ Facebook App ID missing");
                    }


                }

                // 🔁 Preload Ads
                GoogleNativeAdHelper.preload((Activity) context, adsConfig);
                FacebookNativeAdHelper.preload((Activity) context, adsConfig);

                GoogleInterstitialAdHelper.preload(context, adsConfig);
                FacebookInterstitialHelper.preload((Activity) context, adsConfig);

                GoogleRewardedAdHelper.preload(context, adsConfig);
                FacebookRewardedAdHelper.preload((Activity) context, adsConfig);

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
