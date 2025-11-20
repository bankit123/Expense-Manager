package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "recurring_transactions",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Subtype.class,
                        parentColumns = "subtype_id",
                        childColumns = "subtype_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("subtype_id"),
                @Index("category_id")
        }
)
public class RecurringTransaction {

    @PrimaryKey(autoGenerate = true)
    public long recurring_id;

    // 🔗 Foreign keys
    public long user_id;
    public long subtype_id;   // e.g., "UPI", "Credit Card"
    public long category_id;  // e.g., "Entertainment"

    // 💸 Core Transaction Info
    public String title;      // "Netflix", "Rent Payment", etc.
    public double amount;
    public String type;       // "Expense", "Income", or "Transfer"

    // 🔁 Recurrence Settings
    public String frequency;  // "daily", "weekly", "monthly", "yearly"
    public int total_payments;    // total number of repeats (0 for infinite)
    public int completed_payments;// how many completed

    public Date start_date;
    public Date next_due_date;
    public Date end_date;

    // ⚙️ Options
    public String status;            // "active", "paused", "completed"
    public Date last_resume_date;
    public boolean repeat_forever; // true = user selected "Repeat Forever"

    // 🧾 Meta Info
    public String notes;
    public Date created_at;
    public Date updated_at;
}
