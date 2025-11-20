package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


public class CategoryUtil {

    public interface OnCategoryAdded {
        void onAdded();
    }

    // ✅ Register a launcher to handle results from AddCategoryActivity
    public static ActivityResultLauncher<Intent> registerAddCategoryLauncher(
            Activity activity,
            OnCategoryAdded callback
    ) {
        return ((androidx.activity.ComponentActivity) activity).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (callback != null) callback.onAdded();
                    }
                }
        );
    }

    // ✅ Start AddCategoryActivity
    public static void openAddCategory(Context context, boolean isExpense,
                                       ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, AddCategory_Activity.class);
        intent.putExtra("isExpense", isExpense);
        launcher.launch(intent);
    }
}