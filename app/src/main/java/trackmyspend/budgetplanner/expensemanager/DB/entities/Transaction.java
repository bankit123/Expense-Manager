package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "transactions",
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
                        onDelete = ForeignKey.CASCADE ,// ✅ delete transactions when subtype deleted
                        deferred = true
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = RecurringTransaction.class,
                        parentColumns = "recurring_id",
                        childColumns = "recurring_id",
                        onDelete = ForeignKey.CASCADE // ✅ delete all linked transactions if recurring payment is deleted
                )
        },
        indices = {@Index("subtype_id"),
                @Index("category_id"),
                @Index("user_id"),
                @Index("recurring_id")}
)
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public long transaction_id;

    public long user_id;
    public Long subtype_id;
    public Long category_id;
    public Long recurring_id;

    public double amount;
    public String notes;
    public Date date;
    public String source_name;
    public String location;
    public String type; // "Expense", "Income", "Transfer"
    public Long transfer_group_id; // nullable for normal transactions
    public Date created_at;
    public Date updated_at;
}
