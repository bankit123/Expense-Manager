package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void deleteCategory(Category category);

    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories(long userId);

    @Query("SELECT * FROM categories WHERE category_id = :id LIMIT 1")
    Category getCategoryById(long id);

    @Query("SELECT category_id FROM categories WHERE name = :name LIMIT 1")
    long getCategoryIdByName(String name);

    // ✅ All categories for a user
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByUser(long userId);

    // ✅ Filtered categories (Expense or Income)
    @Query("SELECT * FROM categories WHERE user_id = :userId AND type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByUserAndType(long userId, String type);

}
