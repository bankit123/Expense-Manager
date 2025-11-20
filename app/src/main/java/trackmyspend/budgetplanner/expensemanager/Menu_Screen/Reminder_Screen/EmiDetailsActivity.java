package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EmiDetailsActivity extends AppCompatActivity {

    private EMI emi;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emi_details);

        emi = (EMI) getIntent().getSerializableExtra("emi");
        if (emi == null) {
            finish();
            return;
        }

        TextView tvLoan = findViewById(R.id.tvLoanName);
        TextView tvLender = findViewById(R.id.tvLenderName);
        TextView tvEmi = findViewById(R.id.tvEmiAmount);
        TextView tvPaid = findViewById(R.id.tvPaid);
        TextView tvTenure = findViewById(R.id.tvTenure);
        TextView tvStart = findViewById(R.id.tvStartDate);
        TextView tvEnd = findViewById(R.id.tvEndDate);
        TextView tvNext = findViewById(R.id.tvNextDue);
        TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnMarkPaid = findViewById(R.id.btnMarkPaid);

        tvLoan.setText("Loan: " + emi.loan_name);
        tvLender.setText("Lender: " + emi.lender_name);
        tvEmi.setText("EMI: ₹" + String.format(Locale.getDefault(),"%.2f", emi.emi_amount));
        tvPaid.setText("Paid: " + emi.paid_installments + " / " + emi.tenure_months);
        if (emi.start_date != null) tvStart.setText("Start: " + df.format(emi.start_date));
        if (emi.end_date != null) tvEnd.setText("End: " + df.format(emi.end_date));
        if (emi.next_due_date != null) tvNext.setText("Next Due: " + df.format(emi.next_due_date));
        tvStatus.setText("Status: " + emi.status);

        btnMarkPaid.setOnClickListener(v -> markAsPaid());
    }

    private void markAsPaid() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            // 1) Insert a Transaction (adjust to your schema: set user_id, subtype/category if needed)
            Transaction txn = new Transaction();
            txn.user_id = 1; // TODO: replace with real userId logic if needed
            txn.amount = emi.emi_amount;
            txn.notes = "EMI Paid - " + emi.loan_name + " (" + emi.lender_name + ")";
            txn.date = new Date();
            txn.type = "Expense";
            txn.created_at = new Date();
            txn.updated_at = new Date();
            db.transactionDao().insert(txn);

            // 2) Update EMI progress & next due date
            emi.paid_installments++;
            Calendar cal = Calendar.getInstance();
            if (emi.next_due_date != null) {
                cal.setTime(emi.next_due_date);
            } else if (emi.start_date != null) {
                cal.setTime(emi.start_date);
                cal.add(Calendar.MONTH, emi.paid_installments);
            } else {
                cal.setTime(new Date());
            }
            cal.add(Calendar.MONTH, 1);
            emi.next_due_date = cal.getTime();

            if (emi.paid_installments >= emi.tenure_months) {
                emi.status = "Completed";
            }

            db.emiDao().update(emi);

            runOnUiThread(() -> {
                Toast.makeText(this, "Marked as paid", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
