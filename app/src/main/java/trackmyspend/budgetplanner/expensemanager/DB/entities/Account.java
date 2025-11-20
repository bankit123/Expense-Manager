package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "accounts",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Account {
    @PrimaryKey(autoGenerate = true)
    public long account_id;

    public long user_id;
    public String name;

    // Drawable name (like ic_wallet, ic_card, etc.)
    public String icon;

    // ✅ New field for selected color (hex format, e.g. #FF5722)
    public String iconColorHex;

    public double amount;

    public Date created_at;
    public Date updated_at;
}
