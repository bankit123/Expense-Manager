package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Icon;

import java.util.List;

@Dao
public interface IconDao {
    @Insert
    void insert(Icon icon);

    @Query("SELECT * FROM icons")
    List<Icon> getAllIcons();

    @Query("DELETE FROM icons")
    void clear();

    // 🔥 New: filter by Expense / Income / Both
    @Query("SELECT * FROM icons WHERE type = :type OR type = 'Both'")
    List<Icon> getIconsByType(String type);
}
