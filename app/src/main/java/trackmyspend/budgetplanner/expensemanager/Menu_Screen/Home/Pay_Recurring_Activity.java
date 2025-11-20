package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.DayAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.MonthYearPickerBottomSheet;
import trackmyspend.budgetplanner.expensemanager.R;

public class Pay_Recurring_Activity extends AppCompatActivity {

    private TextView tvMonthYear;
    private RecyclerView rvDays;
    private Calendar selectedMonth;
    private DayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_recurring);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        rvDays = findViewById(R.id.rvDays);

        selectedMonth = Calendar.getInstance();
        updateMonthHeader();
        setupDateSelector();

        tvMonthYear.setOnClickListener(v -> showMonthYearPicker());
    }

    /** 🗓️ Updates header like “October 2025” */
    private void updateMonthHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(selectedMonth.getTime()));
    }

    /** 📅 Populate the horizontal day list and auto-select today */
    private void setupDateSelector() {
        List<Calendar> days = new ArrayList<>();
        Calendar calendar = (Calendar) selectedMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        int todayPosition = -1;

        for (int i = 1; i <= maxDay; i++) {
            Calendar day = (Calendar) calendar.clone();
            day.set(Calendar.DAY_OF_MONTH, i);
            days.add(day);

            // ✅ detect today's position for auto-selection
            if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && day.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                todayPosition = i - 1;
            }
        }

        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new DayAdapter(days, day -> {
            // handle on day click if needed
        });
        rvDays.setAdapter(adapter);

        // ✅ highlight and scroll to today's date
        if (todayPosition != -1) {
            adapter.setSelectedDate(days.get(todayPosition));
            rvDays.scrollToPosition(todayPosition);
        }
    }

    /** 🗓️ Show custom BottomSheet Month-Year Picker */
    private void showMonthYearPicker() {
        new MonthYearPickerBottomSheet(
                this,
                selectedMonth.get(Calendar.YEAR),
                selectedMonth.get(Calendar.MONTH),
                (year, month) -> {
                    selectedMonth.set(Calendar.YEAR, year);
                    selectedMonth.set(Calendar.MONTH, month);
                    selectedMonth.set(Calendar.DAY_OF_MONTH, 1);
                    updateMonthHeader();
                    setupDateSelector();
                }
        ).show();
    }
}
