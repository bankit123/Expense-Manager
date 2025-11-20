package trackmyspend.budgetplanner.expensemanager.Util;

import android.content.Context;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CurrencyFormatterUtil {

    // 🔹 Cached user settings
    private static String currencySymbol = "₹";
    private static String localeTag = "en_IN";

    private CurrencyFormatterUtil() {
        // prevent instantiation
    }

    /**
     * Initialize user currency & locale from database once.
     * Call this only ONCE after user data is available (e.g., in MainActivity or after onboarding).
     */
    public static void init(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                User user = db.userDao().getFirstUser();

                if (user != null) {
                    if (user.currency_symbol != null && !user.currency_symbol.isEmpty()) {
                        currencySymbol = user.currency_symbol;
                    }
                    if (user.locale_tag != null && !user.locale_tag.isEmpty()) {
                        localeTag = user.locale_tag;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Returns formatted amount using cached locale & symbol.
     * Example: ₹1,23,456.78 (India) or $12,345.78 (US)
     */
    public static String format(double amount) {
        return formatCurrency(currencySymbol, localeTag, amount);
    }

    /**
     * Original core formatter (still available for manual usage if needed)
     */
    public static String formatCurrency(String currencySymbol, String localeTag, double amount) {
        try {
            Locale locale;
            if (localeTag != null && !localeTag.trim().isEmpty()) {
                String[] parts = localeTag.split("_");
                if (parts.length == 2) {
                    locale = new Locale(parts[0], parts[1]);
                } else {
                    locale = new Locale("en", "IN");
                }
            } else {
                locale = new Locale("en", "IN");
            }

            NumberFormat formatter = NumberFormat.getNumberInstance(locale);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            String formattedAmount = formatter.format(amount);

            return currencySymbol+' '+ formattedAmount;
        } catch (Exception e) {
            return currencySymbol + String.format("%.2f", amount);
        }
    }

    // ✅ Optional: expose getters if you ever need them
    public static String getCurrencySymbol() {
        return currencySymbol;
    }

    public static String getLocaleTag() {
        return localeTag;
    }
}
