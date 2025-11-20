package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransactionSchedule;

@Dao
public interface RecurringScheduleDao {

    // 🟢 Insert a new schedule entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecurringTransactionSchedule schedule);

    // 🟠 Bulk insert (for creating many schedules at once)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecurringTransactionSchedule> schedules);

    // 🟣 Update a schedule (e.g., mark as done/missed)
    @Update
    void update(RecurringTransactionSchedule schedule);

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId AND date(scheduled_date / 1000, 'unixepoch') = date(:date / 1000, 'unixepoch') LIMIT 1")
    RecurringTransactionSchedule getByRecurringIdAndDate(long recurringId, Date date);

    @Query("DELETE FROM recurring_transaction_schedule WHERE recurring_id = :recurringId AND status = 'upcoming'")
    void deleteUpcomingByRecurringId(long recurringId);

    // 🔍 Fetch all schedules for one recurring payment
    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId ORDER BY scheduled_date ASC")
    LiveData<List<RecurringTransactionSchedule>> getSchedulesByRecurringId(long recurringId);

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId AND status = 'upcoming' ORDER BY scheduled_date DESC LIMIT 1")
    RecurringTransactionSchedule getLatestUpcoming(long recurringId);

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId ORDER BY scheduled_date ASC")
    List<RecurringTransactionSchedule> getAllByRecurringId(long recurringId);

    @Update
    void updateSchedules(List<RecurringTransactionSchedule> schedules);

    @Query("SELECT * FROM recurring_transaction_schedule WHERE status = 'upcoming'")
    List<RecurringTransactionSchedule> getAllUpcomingSync();

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId AND date(scheduled_date) = date(:date) LIMIT 1")
    RecurringTransactionSchedule getByRecurringIdAndDateSync(long recurringId, Date date);

    @Query("SELECT * FROM recurring_transaction_schedule WHERE recurring_id = :recurringId ORDER BY scheduled_date ASC")
    List<RecurringTransactionSchedule> getAllByRecurringIdSync(long recurringId);

    @Query("SELECT EXISTS(SELECT 1 FROM recurring_transaction_schedule WHERE recurring_id = :recurringId AND scheduled_date = :scheduledDate LIMIT 1)")
    boolean existsSchedule(long recurringId, Date scheduledDate);


}

