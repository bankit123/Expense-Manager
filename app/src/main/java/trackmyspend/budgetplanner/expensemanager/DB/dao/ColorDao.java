package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import trackmyspend.budgetplanner.expensemanager.DB.entities.ColorEntity;

import java.util.List;

@Dao
public interface ColorDao {
    @Insert
    void insert(ColorEntity color);

    @Query("SELECT * FROM colors")
    List<ColorEntity> getAllColors();

    @Query("DELETE FROM colors")
    void clear();
}