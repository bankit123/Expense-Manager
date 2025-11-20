package trackmyspend.budgetplanner.expensemanager.Profile.Categories;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.Profile.Categories.Adapter.CategoryAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.ArrayList;
import java.util.List;

public class Categories_For_Profile extends AppCompatActivity {

    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private AppDatabase db;
    ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories_for_profile);

        RecyclerView recyclerView = findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        db = AppDatabase.getDatabase(this);

        adapter = new CategoryAdapter(this, categoryList, db);
        recyclerView.setAdapter(adapter);

        // Observe categories
        db.categoryDao().getAllCategories(1) // <-- replace 1 with actual logged-in user_id
                .observe(this, categories -> {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    adapter.notifyDataSetChanged();
                });

    }
}