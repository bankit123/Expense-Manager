package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import android.app.Activity;
import android.view.LayoutInflater;

import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.nativead.NativeAdView;

import trackmyspend.budgetplanner.expensemanager.R;

public class NativeAdViewUtil {

    // 🔵 Google Native Ad View
    public static NativeAdView createGoogleNative(Activity activity) {
        return (NativeAdView) LayoutInflater.from(activity)
                .inflate(R.layout.native_ad_google_video, null);
    }

    // 🔵 Facebook Native Ad Layout
    public static NativeAdLayout createFacebookNative(Activity activity) {
        return (NativeAdLayout) LayoutInflater.from(activity)
                .inflate(R.layout.native_ad_facebook, null);
    }
}
