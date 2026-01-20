package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;

public class MemoryVariable {

    /* -------------------------
       Period Interstitial (session based)
       ------------------------- */
    private static int periodAdFrequency = Integer.parseInt(AdsManager.getConfig().get("period_cnt"));   // set from Firebase
    private static int periodClickCounter = 0;

//       PERIOD INTERSTITIAL LOGIC
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

    /* -------------------------
     ADD TRANSACTION INTERSTITIAL (session based)
     Firebase key: add_tans_ads_cnt
     ------------------------- */
    private static int addTransAdFrequency =
            Integer.parseInt(AdsManager.getConfig().get("add_tans_ads_cnt")); // Firebase
    private static int addTransClickCounter = 0;

    public static void setAddTransAdFrequency(int frequency) {
        addTransAdFrequency = frequency;
    }

    public static boolean shouldShowAddTransactionInterstitial() {
        if (addTransAdFrequency <= 0) return false;

        addTransClickCounter++;

        if (addTransClickCounter >= addTransAdFrequency) {
            addTransClickCounter = 0;
            return true;
        }
        return false;
    }
}
