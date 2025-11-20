package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransactionSchedule;

@Dao
public interface RecurringTransactionDao {

    // 🔹 Insert new recurring transaction
    @Insert
    long insert(RecurringTransaction recurringTransaction);

    // 🔹 Update existing
    @Update
    void update(RecurringTransaction recurringTransaction);

    // 🔹 Delete recurring transaction
    @Delete
    void delete(RecurringTransaction recurringTransaction);

    @Query("SELECT * FROM recurring_transactions")
    List<RecurringTransaction> getAllSync();

    // 🔹 Get recurring transaction by ID (synchronous)
    @Query("SELECT * FROM recurring_transactions WHERE recurring_id = :id LIMIT 1")
    RecurringTransaction getByIdSync(long id);

    @Query("SELECT * FROM recurring_transactions WHERE recurring_id = :recurringId LIMIT 1")
    LiveData<RecurringTransaction> getById(long recurringId);

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId AND status = :status ORDER BY next_due_date ASC")
    LiveData<List<RecurringTransaction>> getRecurringByUserAndStatus(long userId, String status);

    @Query("SELECT * FROM recurring_transactions WHERE status = 'active'")
    List<RecurringTransaction> getAllActiveSync();

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId ORDER BY scheduled_date DESC LIMIT 1")
    RecurringTransactionSchedule getLastScheduleSync(long recurringId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSchedule(RecurringTransactionSchedule schedule);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSchedules(List<RecurringTransactionSchedule> schedules);

    @Update
    void updateRecurring(RecurringTransaction transaction);

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId AND status = :status ORDER BY updated_at DESC")
    LiveData<List<RecurringTransaction>> getAllByStatus(long userId, String status);



}
