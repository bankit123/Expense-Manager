package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.ads.*;

public class FacebookBannerAdHelper {

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            Callback callback) {

        String fbUnit = config.get("fb_banner");
        if (fbUnit == null || fbUnit.isEmpty()) {
            if (callback != null) callback.onFailed();
            return;
        }

        boolean isLarge = "l".equalsIgnoreCase(config.get("banner_size"));

        AdSize fbSize = isLarge
                ? AdSize.RECTANGLE_HEIGHT_250
                : AdSize.BANNER_HEIGHT_50;

        AdView fbAdView = new AdView(activity, fbUnit, fbSize);

//        AdSettings.addTestDevice("1cfdec71-440b-467a-956b-40fe5ea023ec");
        container.removeAllViews();
        container.addView(fbAdView);
        container.setVisibility(View.GONE);

        fbAdView.loadAd(
                fbAdView.buildLoadAdConfig()
                        .withAdListener(new AdListener() {

                            @Override
                            public void onAdLoaded(Ad ad) {
                                container.setVisibility(View.VISIBLE);
                                if (callback != null) callback.onLoaded();
                            }

                            @Override
                            public void onError(Ad ad, AdError error) {
                                container.setVisibility(View.GONE);
                                if (callback != null) callback.onFailed();
                            }

                            @Override public void onAdClicked(Ad ad) {}
                            @Override public void onLoggingImpression(Ad ad) {}
                        })
                        .build()
        );
    }
}
