package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "icons")
public class Icon {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // User-friendly label (e.g. "Food")
    public String displayName;

    // Drawable resource name (e.g. "ic_food")
    public String drawableName;

    // 🔥 New field: type of icon (Expense / Income / Both)
    public String type;
}
