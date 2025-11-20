package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "reminders")
public class Reminder implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long reminder_id;

    public String type;     // "Daily" or "Weekly"
    public String time;     // "HH:mm"
    public int dayOfWeek;   // 1=Sunday...7=Saturday (weekly only)
    public boolean isActive;
}