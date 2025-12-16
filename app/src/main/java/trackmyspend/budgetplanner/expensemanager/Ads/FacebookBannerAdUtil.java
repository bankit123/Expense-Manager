package trackmyspend.budgetplanner.expensemanager.Ads;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.ads.*;

public class FacebookBannerAdUtil {

    private static final String TAG = "FB_BANNER_AD";

    private AdView adView;

    /**
     * Load Facebook Banner Ad
     */
    public void loadBannerAd(
            Activity activity,
            ViewGroup adContainer,
            String placementId
    ) {

        // Initialize SDK once
        AudienceNetworkAds.initialize(activity);

        AdSettings.setTestMode(true);

        // Clear previous ad (important)
        adContainer.removeAllViews();

        adView = new AdView(
                activity,
                placementId,
                AdSize.BANNER_HEIGHT_50
        );

        adContainer.addView(adView);

        adView.loadAd(
                adView.buildLoadAdConfig()
                        .withAdListener(new AdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {
                                Log.d(TAG, "Banner loaded");
                                adContainer.setVisibility(LinearLayout.VISIBLE);
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Log.e(TAG, "Error: " + adError.getErrorMessage());
                                adContainer.setVisibility(LinearLayout.GONE);
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.d(TAG, "Banner clicked");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {}
                        })
                        .build()
        );
    }

    /**
     * Destroy Ad (call in onDestroy)
     */
    public void destroyAd() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }
}
