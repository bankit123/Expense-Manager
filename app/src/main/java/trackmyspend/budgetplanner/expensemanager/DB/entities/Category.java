package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE))
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long category_id;

    public long user_id;
    public String name;
    public String icon;
    public String colorHex;
    public String type; // "Expense" / "Income"
}
