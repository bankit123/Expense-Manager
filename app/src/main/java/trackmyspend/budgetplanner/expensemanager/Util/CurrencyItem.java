package trackmyspend.budgetplanner.expensemanager.Util;

public class CurrencyItem {
    public String symbol;
    public String code;
    public String currencyName;
    public String localeTag; // ✅ Added

    public CurrencyItem(String symbol, String code, String currencyName, String localeTag) {
        this.symbol = symbol;
        this.code = code;
        this.currencyName = currencyName;
        this.localeTag = localeTag;
    }

    // For backward compatibility
    public CurrencyItem(String symbol, String code, String currencyName) {
        this(symbol, code, currencyName, "en_IN");
    }
}


