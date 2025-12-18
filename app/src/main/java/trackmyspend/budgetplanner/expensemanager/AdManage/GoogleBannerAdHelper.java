package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.*;

public class GoogleBannerAdHelper {

    private static final String TAG = "GoogleBannerAd";

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    public static void show(Activity activity,
                            FrameLayout container,
                            AdsConfig config,
                            String bannerSize,
                            Callback callback) {

        String adUnit = config.get("banner");
        if (adUnit == null || adUnit.isEmpty()) {
            if (callback != null) callback.onFailed();
            return;
        }

        boolean isLarge = "l".equalsIgnoreCase(bannerSize);
        Log.d("GoogleBannerAd", "Banner size = " + (isLarge ? "LARGE" : "SMALL"));

        AdView adView = new AdView(activity);
        adView.setAdUnitId(adUnit);
        adView.setAdSize(isLarge ? AdSize.MEDIUM_RECTANGLE : AdSize.BANNER);

        container.removeAllViews();
        container.addView(adView);
        container.setVisibility(View.GONE);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(View.VISIBLE);
                if (callback != null) callback.onLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                container.setVisibility(View.GONE);
                if (callback != null) callback.onFailed();
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

}
