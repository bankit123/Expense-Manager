package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;
import trackmyspend.budgetplanner.expensemanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEmiActivity extends AppCompatActivity {

    private EditText edtLoanName, edtLenderName, edtPrincipal, edtInterestRate, edtTenure, edtPaidInstallments;
    private TextView tvStartDate, tvEmiPreview, btnSaveEmi;
    private final Calendar selectedStartDate = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emi);

        edtLoanName = findViewById(R.id.edtLoanName);
        edtLenderName = findViewById(R.id.edtLenderName);
        edtPrincipal = findViewById(R.id.edtPrincipal);
        edtInterestRate = findViewById(R.id.edtInterestRate);
        edtTenure = findViewById(R.id.edtTenure);
        edtPaidInstallments = findViewById(R.id.edtPaidInstallments);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEmiPreview = findViewById(R.id.tvEmiPreview);
        btnSaveEmi = findViewById(R.id.btnSaveEmi);

        tvStartDate.setText(sdf.format(selectedStartDate.getTime()));
        tvStartDate.setOnClickListener(v -> showDatePicker());

        btnSaveEmi.setOnClickListener(v -> saveEmi());
    }

    private void showDatePicker() {
        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedStartDate.set(year, month, dayOfMonth);
                    tvStartDate.setText(sdf.format(selectedStartDate.getTime()));
                },
                selectedStartDate.get(Calendar.YEAR),
                selectedStartDate.get(Calendar.MONTH),
                selectedStartDate.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void saveEmi() {
        try {
            String loanName = edtLoanName.getText().toString().trim();
            String lenderName = edtLenderName.getText().toString().trim();
            double principal = Double.parseDouble(edtPrincipal.getText().toString().trim());
            double interestRate = Double.parseDouble(edtInterestRate.getText().toString().trim());
            int tenure = Integer.parseInt(edtTenure.getText().toString().trim());
            int paidInstallments = edtPaidInstallments.getText().toString().trim().isEmpty()
                    ? 0 : Integer.parseInt(edtPaidInstallments.getText().toString().trim());

            // Compute EMI
            double r = interestRate / 12.0 / 100.0;
            double emiAmount;
            if (r > 0) {
                double pow = Math.pow(1 + r, tenure);
                emiAmount = (principal * r * pow) / (pow - 1);
            } else {
                emiAmount = principal / tenure;
            }
            tvEmiPreview.setText("EMI: ₹" + String.format(Locale.getDefault(), "%.2f", emiAmount));

            EMI emi = new EMI();
            emi.loan_name = loanName;
            emi.lender_name = lenderName;
            emi.principal_amount = principal;
            emi.interest_rate = interestRate;
            emi.tenure_months = tenure;
            emi.emi_amount = emiAmount;
            emi.paid_installments = Math.max(0, Math.min(paidInstallments, tenure)); // clamp

            emi.start_date = selectedStartDate.getTime();

            // end_date = start + tenure months
            Calendar end = (Calendar) selectedStartDate.clone();
            end.add(Calendar.MONTH, tenure);
            emi.end_date = end.getTime();

            // next_due_date = start + paid_installments months
            Calendar due = (Calendar) selectedStartDate.clone();
            due.add(Calendar.MONTH, emi.paid_installments);
            emi.next_due_date = due.getTime();

            emi.status = (emi.paid_installments >= tenure) ? "Completed" : "Active";

            new Thread(() -> {
                AppDatabase.getDatabase(getApplicationContext()).emiDao().insert(emi);
                runOnUiThread(() -> {
                    Toast.makeText(this, "EMI saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
        }
    }
}
