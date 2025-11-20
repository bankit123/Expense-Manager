package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Adapter.RecurringPaymentAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

public class Recurring_Payment_Activity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView rvRecurringPayments;
    private LinearLayout layoutEmptyState;
    private ImageView ivBack, ivAddRecurringPayment;
    private TextView tvActive, tvPaused, tvCompleted;

    private RecurringPaymentAdapter adapter;
    private final List<RecurringTransaction> recurringList = new ArrayList<>();

    private String selectedStatus = "active"; // Default filter
    private LiveData<List<RecurringTransaction>> currentLiveData; // 🧠 Track current observer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recurring_payment);

        db = AppDatabase.getDatabase(this);

        // 🔗 Bind Views
        ivBack = findViewById(R.id.ivBack);
        ivAddRecurringPayment = findViewById(R.id.ivAddRecurringPayment);
        rvRecurringPayments = findViewById(R.id.rvRecurringPayments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvActive = findViewById(R.id.tvActive);
        tvPaused = findViewById(R.id.tvPaused);
        tvCompleted = findViewById(R.id.tvCompleted);

        // 🔄 Recycler Setup
        rvRecurringPayments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringPaymentAdapter(this, recurringList);
        rvRecurringPayments.setAdapter(adapter);

        // 🔙 Navigation
        ivBack.setOnClickListener(v -> onBackPressed());
        ivAddRecurringPayment.setOnClickListener(v ->
                startActivity(new Intent(this, Add_Recurring_Payment_Activity.class)));

        // ⚙️ Filter Clicks
        tvActive.setOnClickListener(v -> updateFilter("active"));
        tvPaused.setOnClickListener(v -> updateFilter("paused"));
        tvCompleted.setOnClickListener(v -> updateFilter("completed"));

        // 🟩 Default Load
        updateFilter("active");
    }

    /**
     * Dynamically updates the list when a status filter is selected.
     */
    private void updateFilter(String status) {
        selectedStatus = status;
        setStatusFilterSelected(status);

        long userId = 1; // Replace with logged-in user id if applicable

        // 🧹 Remove previous observer before adding a new one
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        currentLiveData = db.recurringTransactionDao().getAllByStatus(userId, status);
        currentLiveData.observe(this, recurringTransactions -> {
            recurringList.clear();
            recurringList.addAll(recurringTransactions);
            adapter.notifyDataSetChanged();

            boolean isEmpty = recurringTransactions.isEmpty();
            layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvRecurringPayments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    /**
     * Highlights the selected filter button (Active / Paused / Completed)
     */
    private void setStatusFilterSelected(String selected) {
        resetSegment(tvActive);
        resetSegment(tvPaused);
        resetSegment(tvCompleted);

        TextView target;
        switch (selected) {
            case "paused":
                target = tvPaused;
                break;
            case "completed":
                target = tvCompleted;
                break;
            default:
                target = tvActive;
        }

        target.setBackgroundResource(R.drawable.bg_segment_selected);
        target.setTextColor(getColor(R.color.nav_icon_active));
    }

    /**
     * Resets segment buttons to default state
     */
    private void resetSegment(TextView view) {
        view.setBackgroundResource(R.drawable.bg_segment_unselected);
        view.setTextColor(getColor(R.color.nav_icon_default));
    }

    /**
     * Refresh data every time the screen is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateFilter(selectedStatus); // Refresh currently selected filter
    }
}
