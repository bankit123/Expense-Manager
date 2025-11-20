package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.*;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Reminder;
import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE isActive = 1")
    List<Reminder> getActiveReminders();

    @Query("SELECT * FROM reminders")
    List<Reminder> getAllReminders();

    @Query("UPDATE reminders SET isActive = :active WHERE reminder_id = :id")
    void setActive(long id, boolean active);

    @Query("SELECT * FROM reminders WHERE reminder_id = :id LIMIT 1")
    Reminder getReminderById(long id);

}
