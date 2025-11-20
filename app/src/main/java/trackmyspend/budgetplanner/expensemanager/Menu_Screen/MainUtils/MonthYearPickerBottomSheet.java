package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Locale;

import trackmyspend.budgetplanner.expensemanager.R;

public class MonthYearPickerBottomSheet extends BottomSheetDialog {

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month);
    }

    public MonthYearPickerBottomSheet(Context context, int initYear, int initMonth, OnDateSelectedListener listener) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_month_year_picker, null);
        setContentView(view);

        NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);
        LinearLayout btnOk = view.findViewById(R.id.btnSave);
        TextView btnToday = view.findViewById(R.id.btnToday);

        // Month Picker setup
        String[] months = new java.text.DateFormatSymbols(Locale.getDefault()).getMonths();
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);

        // Year Picker setup
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 50);
        yearPicker.setMaxValue(currentYear + 50);

        // Default values
        if (initYear == -1 || initMonth == -1) {
            Calendar now = Calendar.getInstance();
            initYear = now.get(Calendar.YEAR);
            initMonth = now.get(Calendar.MONTH);
        }

        monthPicker.setValue(initMonth);
        yearPicker.setValue(initYear);

        // ✅ OK button
        btnOk.setOnClickListener(v -> {
            listener.onDateSelected(yearPicker.getValue(), monthPicker.getValue());
            dismiss();
        });


        // ✅ Today button
        btnToday.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            int todayYear = now.get(Calendar.YEAR);
            int todayMonth = now.get(Calendar.MONTH);

            yearPicker.setValue(todayYear);
            monthPicker.setValue(todayMonth);

            listener.onDateSelected(todayYear, todayMonth);
            dismiss();
        });
    }
}
