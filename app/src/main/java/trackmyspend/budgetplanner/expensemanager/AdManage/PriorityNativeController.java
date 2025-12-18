package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.nativead.NativeAdView;
import com.facebook.ads.NativeAdLayout;

public class PriorityNativeController {

    private static final String TAG = "ADS_NATIVE_PRIORITY";

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            NativeAdView googleNativeView,
                            NativeAdLayout fbNativeLayout) {

        String priority = config.get("priority_native_ads"); // "fb" or "google"

        Log.d(TAG, "SHOW called | priority_native_ads = " + priority);

        if ("fb".equalsIgnoreCase(priority)) {
            Log.d(TAG, "➡ Trying FACEBOOK native first");
            showFacebookFirst(activity, container, config, googleNativeView, fbNativeLayout);
        } else {
            Log.d(TAG, "➡ Trying GOOGLE native first");
            showGoogleFirst(activity, container, config, googleNativeView, fbNativeLayout);
        }
    }

    // ---------------- GOOGLE → FB ----------------

    private static void showGoogleFirst(Activity activity,
                                        FrameLayout container,
                                        AdsConfig config,
                                        NativeAdView googleView,
                                        NativeAdLayout fbLayout) {

        Log.d(TAG, "GOOGLE_NATIVE → show()");

        GoogleNativeAdHelper.show(activity, container, googleView,
                new GoogleNativeAdHelper.Callback() {

                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "✅ GOOGLE_NATIVE loaded & shown");
                        preloadNext(activity, config);
                    }

                    @Override
                    public void onFailed() {
                        Log.w(TAG, "❌ GOOGLE_NATIVE failed → fallback to FACEBOOK_NATIVE");
                        showFacebookFallback(activity, container, config, fbLayout);
                    }
                });
    }

    // ---------------- FB → GOOGLE ----------------

    private static void showFacebookFirst(Activity activity,
                                          FrameLayout container,
                                          AdsConfig config,
                                          NativeAdView googleView,
                                          NativeAdLayout fbLayout) {

        Log.d(TAG, "FACEBOOK_NATIVE → show()");

        FacebookNativeAdHelper.show(activity, container, fbLayout,
                new FacebookNativeAdHelper.Callback() {

                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "✅ FACEBOOK_NATIVE loaded & shown");
                        preloadNext(activity, config);
                    }

                    @Override
                    public void onFailed() {
                        Log.w(TAG, "❌ FACEBOOK_NATIVE failed → fallback to GOOGLE_NATIVE");
                        showGoogleFallback(activity, container, config, googleView);
                    }
                });
    }

    // ---------------- FALLBACKS ----------------

    private static void showGoogleFallback(Activity activity,
                                           FrameLayout container,
                                           AdsConfig config,
                                           NativeAdView googleView) {

        Log.w(TAG, "GOOGLE_NATIVE fallback attempt");

        GoogleNativeAdHelper.show(activity, container, googleView,
                new GoogleNativeAdHelper.Callback() {
                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "✅ GOOGLE_NATIVE loaded in fallback");
                        preloadNext(activity, config);
                    }

                    @Override
                    public void onFailed() {
                        Log.e(TAG, "❌ GOOGLE_NATIVE fallback FAILED");
                    }
                });
    }

    private static void showFacebookFallback(Activity activity,
                                             FrameLayout container,
                                             AdsConfig config,
                                             NativeAdLayout fbLayout) {

        Log.w(TAG, "FACEBOOK_NATIVE fallback attempt");

        FacebookNativeAdHelper.show(activity, container, fbLayout,
                new FacebookNativeAdHelper.Callback() {
                    @Override
                    public void onLoaded() {
                        Log.d(TAG, "✅ FACEBOOK_NATIVE loaded in fallback");
                        preloadNext(activity, config);
                    }

                    @Override
                    public void onFailed() {
                        Log.e(TAG, "❌ FACEBOOK_NATIVE fallback FAILED");
                    }
                });
    }

    // ---------------- PRELOAD NEXT ----------------

    private static void preloadNext(Activity activity, AdsConfig config) {

        Log.d(TAG, "🔄 Preloading NEXT native ads (Google + Facebook)");

        GoogleNativeAdHelper.preload(activity, config);
        FacebookNativeAdHelper.preload(activity, config);
    }
}
