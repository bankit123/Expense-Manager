package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.concurrent.Executors;

public class AddCategory_Activity extends AppCompatActivity {

    private EditText etNewCategory;
    private FrameLayout iconContainer;
    private ImageView ivCategoryIcon;
    private TextView tvCategoryLetter;
    private RecyclerView rvIcons, rvColors;
    private LinearLayout btnAddCategory;
    ImageView ivBack;
    private AppDatabase db;
    private boolean isExpense;
    private String selectedDrawableName = null;
    private String selectedColorHex = null;
    private String selectedIconName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_category);

        db = AppDatabase.getDatabase(this);
        isExpense = getIntent().getBooleanExtra("isExpense", true);

        // UI
        etNewCategory = findViewById(R.id.etNewCategory);
        iconContainer = findViewById(R.id.iconContainer);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        tvCategoryLetter = findViewById(R.id.tvCategoryLetter);
        rvIcons = findViewById(R.id.rvIcons);
        rvColors = findViewById(R.id.rvColors);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        ivBack = findViewById(R.id.ivBack);

        // Back
        ivBack.setOnClickListener(v -> finish());

        FrameLayout bannerContainer = findViewById(R.id.banner_container);
        AdsManager.loadBanner(this, bannerContainer);

        // Setup icon + color pickers
        CategoryIconColorPickerUtil.setupIconAndColorPicker(
                this,
                iconContainer,
                ivCategoryIcon,
                rvIcons,
                rvColors,
                isExpense,
                (iconName, drawableName, colorHex) -> {
                    selectedIconName = iconName;
                    selectedDrawableName = drawableName;
                    selectedColorHex = colorHex;
                }
        );

        // Save
        btnAddCategory.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = etNewCategory.getText().toString().trim();
        if (name.isEmpty() || selectedDrawableName == null || selectedColorHex == null) {
            Toast.makeText(this, "Enter name & select icon + color", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = new Category();
        category.user_id = 1;
        category.name = name;
        category.icon = selectedDrawableName;
        category.colorHex = selectedColorHex;
        category.type = isExpense ? "Expense" : "Income";

        Executors.newSingleThreadExecutor().execute(() -> {
            db.categoryDao().insert(category);
            runOnUiThread(() -> {
                setResult(Activity.RESULT_OK); // ✅ Return success
                finish();
            });
        });
    }
}