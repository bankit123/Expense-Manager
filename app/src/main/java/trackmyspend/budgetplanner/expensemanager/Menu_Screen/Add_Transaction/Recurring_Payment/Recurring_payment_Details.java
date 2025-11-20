package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Adapter.RecurringScheduleAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

public class Recurring_payment_Details extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvTitle, tvAmount, tvFrequency, tvNextDue, tvStartDate, tvEndDate, tvNotes;
    private LinearLayout btnShowTransaction;
    private long recurringId;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private RecurringTransaction transaction;
    private BottomSheetDialog scheduleDialog; // 👈 hold reference to bottom sheet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recurring_payment_details);

        db = AppDatabase.getDatabase(this);
        recurringId = getIntent().getLongExtra("recurring_id", -1);

        // 🔗 Bind views
        tvTitle = findViewById(R.id.tvRecurringTitle);
        tvAmount = findViewById(R.id.tvRecurringAmount);
        tvFrequency = findViewById(R.id.tvRecurringFrequency);
        tvNextDue = findViewById(R.id.tvRecurringNextDue);
        tvStartDate = findViewById(R.id.tvRecurringStart);
        tvEndDate = findViewById(R.id.tvRecurringEnd);
        tvNotes = findViewById(R.id.tvNotes);
        btnShowTransaction = findViewById(R.id.btnShowTransaction);

        btnShowTransaction.setOnClickListener(v -> showScheduleBottomSheet());
        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        loadRecurringDetails();
    }

    /**
     * 🔽 Show recurring schedules in a bottom sheet
     */
    private void showScheduleBottomSheet() {
        // ✅ Create dialog
        scheduleDialog = new BottomSheetDialog(this);

        // ✅ Inflate layout
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottomsheet_schedules, null);
        scheduleDialog.setContentView(sheetView);

        // 🔹 Initialize views
        TextView tvStatus = sheetView.findViewById(R.id.tvBottomSheetStatus);
        RecyclerView rvSheet = sheetView.findViewById(R.id.rvBottomSheetSchedules);

        // 🔹 Setup RecyclerView
        rvSheet.setLayoutManager(new LinearLayoutManager(this));
        RecurringScheduleAdapter adapter = new RecurringScheduleAdapter(this);
        rvSheet.setAdapter(adapter);

        // 🔹 Show current transaction status
        if (transaction != null) {
            tvStatus.setText(capitalize(transaction.status));
        }

        // 🔹 Observe schedule list
        new Thread(() -> {
            RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(recurringId);
            ((Activity) this).runOnUiThread(() -> {
                db.recurringScheduleDao().getSchedulesByRecurringId(recurringId).observe(this, list -> {
                    boolean canShowPay = rt == null || rt.total_payments == 0 || rt.completed_payments < rt.total_payments;
                    adapter.setData(list != null ? list : Collections.emptyList());

                });
            });
        }).start();

        // ✅ Show sheet
        scheduleDialog.show();
    }

    /**
     * 🔹 Public method called by adapter to close the sheet after action
     */
    public void dismissScheduleBottomSheet() {
        if (scheduleDialog != null && scheduleDialog.isShowing()) {
            scheduleDialog.dismiss();
        }
    }

    /**
     * 🔹 Load transaction details and populate UI
     */
    /**
     * 🔹 Load transaction details and populate UI
     */
    private void loadRecurringDetails() {
        db.recurringTransactionDao().getById(recurringId).observe(this, transaction -> {
            if (transaction != null) {
                this.transaction = transaction;
                tvTitle.setText(transaction.title);
                tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", transaction.amount));
                tvFrequency.setText(capitalize(transaction.frequency));
                tvNextDue.setText(transaction.next_due_date != null ? sdf.format(transaction.next_due_date) : "N/A");
                tvStartDate.setText(sdf.format(transaction.start_date));
                tvEndDate.setText(transaction.end_date == null ? "No end date" : sdf.format(transaction.end_date));
                tvNotes.setText(transaction.notes != null ? transaction.notes : "No notes");

                TextView tvRepeatForeverLabel = findViewById(R.id.tvRepeatForeverLabel);
                TextView tvPaymentCount = findViewById(R.id.tvPaymentCount);

                if (transaction.repeat_forever) {
                    tvRepeatForeverLabel.setText("Repeat Forever: ON");
                    tvPaymentCount.setText("Unlimited");
                } else {
                    tvRepeatForeverLabel.setText("Repeat Forever: OFF");
                    tvPaymentCount.setText(String.valueOf(transaction.total_payments));
                }

                // 🟡 Pause/Resume section
                LinearLayout pauseSection = findViewById(R.id.pauseSection);
                MaterialSwitch switchPause = findViewById(R.id.switchPauseRecurring);
                TextView tvPauseHelp = findViewById(R.id.tvPauseHelp);

                // ✅ Hide pause section if status = completed
                if ("completed".equalsIgnoreCase(transaction.status)) {
                    if (pauseSection != null) {
                        pauseSection.setVisibility(View.GONE);
                    }
                    return; // stop further setup
                } else {
                    if (pauseSection != null) {
                        pauseSection.setVisibility(View.VISIBLE);
                    }
                }

                if (switchPause != null && tvPauseHelp != null) {
                    switchPause.setOnCheckedChangeListener(null); // 🧠 temporarily remove listener

                    switchPause.setChecked("active".equalsIgnoreCase(transaction.status));
                    tvPauseHelp.setText(transaction.status.equalsIgnoreCase("paused")
                            ? "Recurring payments are paused. Turn this on to resume."
                            : "Turn this off to temporarily stop upcoming payments.");

                    // ✅ reattach listener AFTER setting state
                    switchPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        String newStatus = isChecked ? "active" : "paused";
                        updateRecurringStatus(transaction.recurring_id, newStatus);

                        if (isChecked) {
                            tvPauseHelp.setText("Turn this off to temporarily stop upcoming payments.");
                        } else {
                            tvPauseHelp.setText("Recurring payments are paused. Turn this on to resume.");
                        }
                    });
                }
            }
        });
    }


    /**
     * 🔹 Update recurring transaction status (active / paused)
     */
    private void updateRecurringStatus(long recurringId, String newStatus) {
        new Thread(() -> {
            try {
                RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(recurringId);
                if (rt != null) {
                    rt.status = newStatus;
                    rt.updated_at = new Date();

                    // ✅ Set resume date only when resuming
                    if ("active".equalsIgnoreCase(newStatus)) {
                        rt.last_resume_date = new Date();
                    }

                    db.recurringTransactionDao().update(rt);

                    runOnUiThread(() -> {
                        if ("paused".equalsIgnoreCase(newStatus)) {
                            Toast.makeText(this, "Recurring payments paused ⏸️", Toast.LENGTH_SHORT).show();
                        } else if ("active".equalsIgnoreCase(newStatus)) {
                            Toast.makeText(this, "Recurring payments resumed ▶️", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
