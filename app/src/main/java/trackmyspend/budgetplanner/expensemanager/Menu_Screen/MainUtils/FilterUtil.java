package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import android.content.Context;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;

public class FilterUtil {

    private static final SimpleDateFormat RANGE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // Interface for passing results back
    public interface OnDateRangeSelectedListener {
        void onDateRangeSelected(Date start, Date end, String label);
    }

    // Normalize start of day
    private static void normalizeStart(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    // Normalize end of day
    private static void normalizeEnd(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    // 🔹 All Transactions (no filter)
// 🔹 All Transactions (no date filter)
    public static void getAllRange(OnDateRangeSelectedListener listener) {
        // Pass nulls to indicate no date filtering
        listener.onDateRangeSelected(null, null, "All Transactions");
    }


    // 🔹 Weekly Filter
    public static void getWeeklyRange(OnDateRangeSelectedListener listener) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        normalizeStart(cal);
        Date start = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        normalizeEnd(cal);
        Date end = cal.getTime();

        listener.onDateRangeSelected(start, end,
                RANGE_FORMAT.format(start) + " - " + RANGE_FORMAT.format(end));
    }

    // 🔹 Monthly Filter
    public static void getMonthlyRange(OnDateRangeSelectedListener listener) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        normalizeStart(cal);
        Date start = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        normalizeEnd(cal);
        Date end = cal.getTime();

        listener.onDateRangeSelected(start, end,
                RANGE_FORMAT.format(start) + " - " + RANGE_FORMAT.format(end));
    }

    // 🔹 Yearly Filter
    public static void getYearlyRange(OnDateRangeSelectedListener listener) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_YEAR, 1);
        normalizeStart(cal);
        Date start = cal.getTime();

        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        normalizeEnd(cal);
        Date end = cal.getTime();

        listener.onDateRangeSelected(start, end,
                RANGE_FORMAT.format(start) + " - " + RANGE_FORMAT.format(end));
    }

    // 🔹 Period Picker (Custom Range)
    public static void showPeriodPicker(Context context, androidx.fragment.app.FragmentManager fm,
                                        OnDateRangeSelectedListener listener) {

        AdsManager.showInterstitial((android.app.Activity) context);

        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Period");
        builder.setCalendarConstraints(new CalendarConstraints.Builder().build());

        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.show(fm, picker.toString());

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                long startMillis = selection.first;
                long endMillis = selection.second;

                Calendar startCal = Calendar.getInstance();
                startCal.setTimeInMillis(startMillis);
                normalizeStart(startCal);
                Date startDate = startCal.getTime();

                Calendar endCal = Calendar.getInstance();
                endCal.setTimeInMillis(endMillis);
                normalizeEnd(endCal);
                Date endDate = endCal.getTime();

                String label = "From " + RANGE_FORMAT.format(startDate) +
                        " - " + RANGE_FORMAT.format(endDate);

                listener.onDateRangeSelected(startDate, endDate, label);
            }
        });
    }
}
