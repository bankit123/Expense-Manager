package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * @description Defines each expected occurrence (cycle) of a RecurringTransaction.
 * Tracks whether each payment was done, missed, or upcoming.
 * Links RecurringTransaction ↔ Transaction (actual record).
 */
@Entity(
        tableName = "recurring_transaction_schedule",
        foreignKeys = {
                @ForeignKey(
                        entity = RecurringTransaction.class,
                        parentColumns = "recurring_id",
                        childColumns = "recurring_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Transaction.class,
                        parentColumns = "transaction_id",
                        childColumns = "transaction_id",
                        onDelete = ForeignKey.SET_NULL // keep schedule even if transaction is deleted
                )
        },
        indices = {
                @Index("recurring_id"),
                @Index("transaction_id"),
                @Index("scheduled_date")
        }
)
public class RecurringTransactionSchedule {

    @PrimaryKey(autoGenerate = true)
    public long schedule_id;

    // 🔗 Foreign key relationships
    public long recurring_id;      // FK → RecurringTransaction
    public Long transaction_id;    // FK → Transaction (nullable until payment done)

    // 📅 Schedule details
    public Date scheduled_date;    // expected payment date
    public double amount;          // snapshot of amount for this occurrence
    public String status;          // "upcoming", "completed", "missed"
    public String notes;           // optional context, e.g. "Missed due to insufficient balance"

    // 🕓 Audit timestamps
    public Date created_at;
    public Date updated_at;

    // 🧩 Convenience constructor (optional)
    public RecurringTransactionSchedule(long recurring_id, Date scheduled_date, double amount, String status) {
        this.recurring_id = recurring_id;
        this.scheduled_date = scheduled_date;
        this.amount = amount;
        this.status = status;
        this.created_at = new Date();
        this.updated_at = new Date();
    }

    // ⚙️ Empty constructor required by Room
    public RecurringTransactionSchedule() {}
}

