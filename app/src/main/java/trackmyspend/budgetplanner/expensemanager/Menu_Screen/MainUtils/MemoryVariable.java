package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;

public class MemoryVariable {

    /* -------------------------
       Session flags (reset when app is killed)
       ------------------------- */
    private static boolean supportDialogShown = false;

    /* -------------------------
       Period Interstitial (session based)
       ------------------------- */
    private static int periodAdFrequency = Integer.parseInt(AdsManager.getConfig().get("period_cnt"));   // set from Firebase
    private static int periodClickCounter = 0;

    /* -------------------------
       SUPPORT DIALOG (SESSION ONLY)
       ------------------------- */
    public static boolean isSupportDialogShown() {
        return supportDialogShown;
    }

    public static void setSupportDialogShown(boolean shown) {
        supportDialogShown = shown;
    }

    /* -------------------------
       PERIOD INTERSTITIAL LOGIC
       ------------------------- */
    public static void setPeriodAdFrequency(int frequency) {
        periodAdFrequency = frequency;
    }

    public static boolean shouldShowPeriodInterstitial() {
        if (periodAdFrequency <= 0) return false;

        periodClickCounter++;

        if (periodClickCounter >= periodAdFrequency) {
            periodClickCounter = 0;
            return true;
        }
        return false;
    }
}
