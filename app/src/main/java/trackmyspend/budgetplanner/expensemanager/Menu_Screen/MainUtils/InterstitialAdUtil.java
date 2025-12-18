package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;
import android.app.Activity;
import android.widget.Toast;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleInterstitialAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityInterstitialController;

public class InterstitialAdUtil {

    public static void showWithToast(Activity activity) {

        if (AdsManager.getConfig() == null) {
            Toast.makeText(activity, "Ads not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        PriorityInterstitialController.show(
                activity,
                AdsManager.getConfig(),
                new GoogleInterstitialAdHelper.Callback() {

                    @Override
                    public void onShown() {
                        Toast.makeText(
                                activity,
                                "Interstitial Shown",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onDismissed() {
                        Toast.makeText(
                                activity,
                                "Interstitial Closed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(
                                activity,
                                "No Interstitial Available",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onNotReady() {
                        Toast.makeText(
                                activity,
                                "Interstitial Not Ready",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
