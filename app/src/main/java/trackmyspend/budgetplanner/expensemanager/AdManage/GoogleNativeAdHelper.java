package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import trackmyspend.budgetplanner.expensemanager.R;

public class GoogleNativeAdHelper {

    private static final String TAG = "ADS_NATIVE_GOOGLE";
    private static NativeAd nativeAd;

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    // ---------------- PRELOAD ----------------

    public static void preload(Activity activity, AdsConfig config) {

        String adUnit = config.get("native");
        if (adUnit == null || adUnit.isEmpty()) {
            Log.w(TAG, "Preload skipped → native id missing");
            return;
        }

        Log.d(TAG, "Preload started");

        AdLoader adLoader = new AdLoader.Builder(activity, adUnit)
                .forNativeAd(ad -> {
                    nativeAd = ad;
                    Log.d(TAG, "Native loaded");

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        nativeAd = null;
                        Log.e(TAG,
                                "Native failed | code=" + error.getCode()
                                        + " msg=" + error.getMessage());
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    // ---------------- SHOW ----------------

    public static void show(Activity activity,
                            FrameLayout container,
                            NativeAdView adView,
                            Callback callback) {

        if (nativeAd == null) {
            Log.w(TAG, "Show failed → ad is NULL");
            if (callback != null) callback.onFailed();
            return;
        }

        Log.d(TAG, "Rendering native ad");

        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(View.VISIBLE);

        // ---------------- FIND VIEWS ----------------

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        TextView headline = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        Button cta = adView.findViewById(R.id.ad_cta);
        ImageView icon = adView.findViewById(R.id.ad_icon);

        if (mediaView == null || headline == null || cta == null) {
            Log.e(TAG, "Show failed → layout missing required views");
            if (callback != null) callback.onFailed();
            return;
        }

        // ---------------- BIND DATA ----------------

        headline.setText(nativeAd.getHeadline());
        body.setText(nativeAd.getBody());
        cta.setText(nativeAd.getCallToAction());

        // Icon (optional but recommended)
        if (nativeAd.getIcon() != null && icon != null) {
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());
            icon.setVisibility(View.VISIBLE);
            adView.setIconView(icon);
        } else if (icon != null) {
            icon.setVisibility(View.GONE);
        }

        // ---------------- REGISTER VIEWS ----------------

        adView.setMediaView(mediaView);
        adView.setHeadlineView(headline);
        adView.setBodyView(body);
        adView.setCallToActionView(cta);


        adView.setNativeAd(nativeAd);

        Log.d(TAG, "Native shown successfully");

        if (callback != null) callback.onLoaded();

        // Consume once
        nativeAd = null;
    }
}
