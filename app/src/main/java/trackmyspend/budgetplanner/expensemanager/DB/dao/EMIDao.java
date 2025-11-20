package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.*;
import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;

import java.util.Date;
import java.util.List;

@Dao
public interface EMIDao {
    @Insert
    long insert(EMI emi);

    @Update
    void update(EMI emi);

    @Delete
    void delete(EMI emi);

    @Query("SELECT * FROM emis WHERE status = 'Active'")
    List<EMI> getActiveEMIsSync();

    @Query("SELECT * FROM emis WHERE emi_id = :id LIMIT 1")
    EMI getById(long id);

    // All EMIs with due date today or earlier and still Active
    @Query("SELECT * FROM emis WHERE status = 'Active' AND next_due_date <= :today")
    List<EMI> getDueEmis(Date today);
}