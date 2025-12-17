package trackmyspend.budgetplanner.expensemanager.AdManage;

import java.util.HashMap;
import java.util.Map;

public class AdsConfig {

    private final Map<String, String> data = new HashMap<>();

    public void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(data.get(key));
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
