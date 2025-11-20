package trackmyspend.budgetplanner.expensemanager.DB.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(
        tableName = "emis",
        foreignKeys = {
                @ForeignKey(
                        entity = Subtype.class,
                        parentColumns = "subtype_id",
                        childColumns = "subtype_id",
                        onDelete = ForeignKey.SET_NULL
                ),
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.SET_NULL
                )
        }
)
public class EMI implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long emi_id;

    // Loan Details
    public String loan_name;       // e.g., "Car Loan", "Personal Loan"
    public String lender_name;     // Bank / NBFC

    public double principal_amount; // Total loan amount
    public double interest_rate;    // Annual % rate
    public int tenure_months;       // Loan duration in months
    public double emi_amount;       // Fixed monthly EMI (pre-calculated)

    public Date start_date;        // Loan start
    public Date end_date;          // Auto-calculated = start + tenure
    public Date next_due_date;        // next EMI date

    public String status;          // Active / Completed / Default
    public int paid_installments;  // number of installments already paid

    // Links to existing Subtype + Category (nullable)
    public Long subtype_id;        // optional → link to account subtype
    public Long category_id;       // optional → link to category
}
