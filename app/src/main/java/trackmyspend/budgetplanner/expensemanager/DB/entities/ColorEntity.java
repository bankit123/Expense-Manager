package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "colors")
public class ColorEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Hex color code (e.g. "#FF9800")
    public String hex;
}