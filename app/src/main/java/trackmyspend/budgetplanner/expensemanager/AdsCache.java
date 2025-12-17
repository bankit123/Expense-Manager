package trackmyspend.budgetplanner.expensemanager;

import android.view.View;

/**
 * Simple in-memory cache for ads loaded in Splash
 */
public class AdsCache {

    private static View bannerView;

    public static void setBanner(View view) {
        bannerView = view;
    }

    public static View getBanner() {
        return bannerView;
    }

    public static void clear() {
        bannerView = null;
    }
}
