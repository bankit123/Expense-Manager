package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public long user_id;

    public String name;
    public String currency_name;   // e.g. Indian Rupee
    public String currency_code;   // e.g. INR
    public String currency_symbol; // e.g. ₹
    public String locale_tag;      // ✅ e.g. "en_IN" or "en_US"
    public String mode;
    public Date created_at;
    public Date updated_at;
}

