package trackmyspend.budgetplanner.expensemanager.AdManage;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class AdsConfigRepository {

    public interface Callback {
        void onSuccess(AdsConfig config);
        void onFailure();
    }

    public static void fetch(Callback callback) {

        FirebaseDatabase.getInstance()
                .getReference("test_ads")
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        callback.onFailure();
                        return;
                    }

                    AdsConfig config = new AdsConfig();
                    DataSnapshot root = task.getResult();

                    for (DataSnapshot section : root.getChildren()) {
                        for (DataSnapshot child : section.getChildren()) {
                            config.put(
                                    child.getKey(),
                                    String.valueOf(child.getValue())
                            );
                        }
                    }

                    if (config.isEmpty()) {
                        callback.onFailure();
                    } else {
                        callback.onSuccess(config);
                    }
                });
    }
}
