package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "subtypes",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "account_id",
                childColumns = "account_id",
                onDelete = ForeignKey.CASCADE // ✅ delete all subtypes when account deleted
        ),
        indices = {@Index("account_id")}
)
public class Subtype {
    @PrimaryKey(autoGenerate = true)
    public long subtype_id;

    public long account_id;   // parent link
    public String name;
    public String icon;
    public String backgroundColorHex;
}
