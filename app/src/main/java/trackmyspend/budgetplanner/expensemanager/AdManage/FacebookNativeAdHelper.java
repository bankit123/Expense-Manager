package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.ads.*;

import java.util.Arrays;
import java.util.List;

import trackmyspend.budgetplanner.expensemanager.R;

public class FacebookNativeAdHelper {

    private static final String TAG = "ADS_NATIVE_FB";
    private static NativeAd fbNativeAd;

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    // ---------------- PRELOAD ----------------

    public static void preload(Activity activity, AdsConfig config) {

        String fbUnit = config.get("fb_native");
        if (fbUnit == null || fbUnit.isEmpty()) {
            Log.w(TAG, "Preload skipped → fb_native id missing");
            return;
        }

        Log.d(TAG, "Preload started");

        fbNativeAd = new NativeAd(activity, fbUnit);

        fbNativeAd.loadAd(
                fbNativeAd.buildLoadAdConfig()
                        .withAdListener(new NativeAdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {
                                Log.d(TAG, "Native loaded");
                            }

                            @Override
                            public void onError(Ad ad, AdError error) {
                                Log.e(TAG,
                                        "Native failed | code=" + error.getErrorCode()
                                                + " msg=" + error.getErrorMessage());
                                fbNativeAd = null;
                            }

                            @Override public void onMediaDownloaded(Ad ad) {}
                            @Override public void onAdClicked(Ad ad) {}
                            @Override public void onLoggingImpression(Ad ad) {}
                        })
                        .build()
        );
    }

    // ---------------- SHOW ----------------

    public static void show(Activity activity,
                            FrameLayout container,
                            NativeAdLayout nativeLayout,
                            Callback callback) {

        if (fbNativeAd == null) {
            Log.w(TAG, "Show failed → ad is NULL");
            if (callback != null) callback.onFailed();
            return;
        }

        if (!fbNativeAd.isAdLoaded()) {
            Log.w(TAG, "Show failed → ad not loaded");
            if (callback != null) callback.onFailed();
            return;
        }

        Log.d(TAG, "Rendering native ad");

        container.removeAllViews();
        container.addView(nativeLayout);
        container.setVisibility(View.VISIBLE);

        MediaView mediaView = nativeLayout.findViewById(R.id.ad_media);
        MediaView iconView = nativeLayout.findViewById(R.id.ad_icon);
        TextView title = nativeLayout.findViewById(R.id.ad_title);
        TextView body = nativeLayout.findViewById(R.id.ad_body);
        Button cta = nativeLayout.findViewById(R.id.ad_cta);

        if (mediaView == null || title == null || cta == null) {
            Log.e(TAG, "Show failed → layout missing required views");
            if (callback != null) callback.onFailed();
            return;
        }

        title.setText(fbNativeAd.getAdvertiserName());
        body.setText(fbNativeAd.getAdBodyText());

        if (fbNativeAd.hasCallToAction()) {
            cta.setText(fbNativeAd.getAdCallToAction());
            cta.setVisibility(View.VISIBLE);
        } else {
            cta.setVisibility(View.GONE);
        }

        AdOptionsView adOptionsView =
                new AdOptionsView(activity, fbNativeAd, nativeLayout);

        ViewGroup adChoicesContainer =
                nativeLayout.findViewById(R.id.ad_choices_container);

        if (adChoicesContainer != null) {
            adChoicesContainer.removeAllViews();
            adChoicesContainer.addView(adOptionsView);
        }

        fbNativeAd.unregisterView();

        List<View> clickableViews = Arrays.asList(title, cta, mediaView);

        fbNativeAd.registerViewForInteraction(
                nativeLayout,
                mediaView,
                iconView,
                clickableViews
        );

        Log.d(TAG, "Native shown successfully");

        if (callback != null) callback.onLoaded();

        fbNativeAd = null;
    }
}
