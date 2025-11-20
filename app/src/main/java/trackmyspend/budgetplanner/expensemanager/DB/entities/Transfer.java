package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "transfers",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Transaction.class,
                        parentColumns = "transaction_id",
                        childColumns = "from_transaction_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Transaction.class,
                        parentColumns = "transaction_id",
                        childColumns = "to_transaction_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class Transfer {
    @PrimaryKey(autoGenerate = true)
    public long transfer_id;

    public long user_id;
    public long from_transaction_id;
    public long to_transaction_id;
    public String notes;
    public Date created_at;
}
