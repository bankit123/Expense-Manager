package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransactionSchedule;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Account_Subtype.SubtypePickerUtil;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter.CategoryAdapter;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.ReviewUtils;

public class Add_Recurring_Payment_Activity extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvSpend, tvDateTime, tvIncome, tvCategory, tvSubtype, tvFrequency, tvStartDate, tvEndDate, tvRepeatHelp;
    private EditText etAmount, etTitle, etNotes, etPaymentCount;
    private ImageView ivCategoryIcon, ivSubtypeIcon, ivCalendar;
    private MaterialSwitch switchRepeatForever;
    private LinearLayout layoutDateTime, layoutCategory, layoutSubtype, layoutFrequency, layoutPaymentCount, btnSave;

    private boolean isExpense = true;
    private boolean isEditing = false;
    private long recurringId = -1;
    private long selectedCategoryId = -1;
    private long selectedSubtypeId = -1;
    private String selectedFrequency = null;

    private Date selectedStartDate = new Date();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private boolean isCategorySheetOpenedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_recurring_payment);

        db = AppDatabase.getDatabase(this);

        // 🔗 Bind views
        tvSpend = findViewById(R.id.tvSpend);
        tvDateTime = findViewById(R.id.tvDateTime);
        ivCalendar = findViewById(R.id.ivCalendar);
        layoutDateTime = findViewById(R.id.layoutDateTime); // if you give the parent LinearLayout an id
        tvIncome = findViewById(R.id.tvIncome);
        tvCategory = findViewById(R.id.tvCategory);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        tvSubtype = findViewById(R.id.tvSubtype);
        ivSubtypeIcon = findViewById(R.id.ivSubtypeIcon);
        layoutCategory = findViewById(R.id.layoutCategory);
        layoutSubtype = findViewById(R.id.layoutSubtype);
        layoutFrequency = findViewById(R.id.layoutFrequency);
        tvFrequency = findViewById(R.id.tvFrequency);
        etAmount = findViewById(R.id.etAmount);
        etTitle = findViewById(R.id.etTitle);
        etNotes = findViewById(R.id.etNotes);
        etPaymentCount = findViewById(R.id.etPaymentCount);
        switchRepeatForever = findViewById(R.id.switchRepeatForever);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        layoutPaymentCount = findViewById(R.id.layoutPaymentCount);
        tvRepeatHelp = findViewById(R.id.tvRepeatHelp);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        // Default selection: Expense
        setFilterSelected(tvSpend, tvIncome);
        tvSpend.setOnClickListener(v -> {
            isExpense = true;
            setFilterSelected(tvSpend, tvIncome);
        });
        tvIncome.setOnClickListener(v -> {
            isExpense = false;
            setFilterSelected(tvIncome, tvSpend);
        });

        layoutFrequency.setOnClickListener(v -> showFrequencyBottomSheet());
        layoutCategory.setOnClickListener(v -> showCategoryBottomSheet(1));
        layoutSubtype.setOnClickListener(v -> setupSubtypePicker());

        View.OnClickListener dateClickListener = v -> openDatePicker(tvDateTime);
        ivCalendar.setOnClickListener(dateClickListener);
        layoutDateTime.setOnClickListener(dateClickListener);
        tvDateTime.setOnClickListener(dateClickListener);


        switchRepeatForever.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etPaymentCount.setEnabled(!isChecked);
            layoutPaymentCount.setAlpha(isChecked ? 0.4f : 1f);
            if (isChecked) {
                etPaymentCount.setText("");
                tvRepeatHelp.setText("This recurring payment will continue until you stop it manually.");
                tvEndDate.setVisibility(View.GONE);
            } else {
                tvRepeatHelp.setText("Specify how many times this payment should repeat.");
                tvEndDate.setVisibility(View.VISIBLE);
                calculateEndDate();
            }
        });

        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                etTitle.requestFocus(); // 👈 move to next EditText
                return true;
            }
            return false;
        });

        // ✅ When user presses Done on Amount field → move to Title (keep keyboard open)
        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                etTitle.requestFocus(); // move focus to next field
                return true;
            }
            return false;
        });


// ✅ When user presses Done on Title field → close keyboard + open Category (only once)
        etTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                // ✅ Close keyboard safely
                v.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                // ✅ Scroll to top (optional for smooth UX)
                v.postDelayed(() -> {
                    View scrollView = findViewById(R.id.scrollView);
                    if (scrollView instanceof android.widget.ScrollView) {
                        ((android.widget.ScrollView) scrollView).smoothScrollTo(0, 0);
                    }
                }, 100);

                // ✅ Open Category bottom sheet only the first time for new transactions
                if (!isCategorySheetOpenedOnce) {
                    isCategorySheetOpenedOnce = true;
                    v.postDelayed(() -> showCategoryBottomSheet(1), 150);
                }

                return true;
            }
            return false;
        });




        etPaymentCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateEndDate();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSave.setOnClickListener(v -> saveOrUpdateRecurringPayment());

        recurringId = getIntent().getLongExtra("recurring_id", -1);
        if (recurringId != -1) {
            isEditing = true;
            loadRecurringPayment(recurringId);
        }
    }

    private void openDatePicker(TextView tvDateTime) {
        final Calendar calendar = Calendar.getInstance();

        // Start from current selectedStartDate if available
        if (selectedStartDate != null) {
            calendar.setTime(selectedStartDate);
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedStartDate = calendar.getTime();

                    // 🕒 Format date with time
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
                    tvDateTime.setText(displayFormat.format(selectedStartDate));

                    // Sync your start date label too
                    tvStartDate.setText(sdf.format(selectedStartDate));
                    calculateEndDate();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // 🚫 Restrict past dates
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePicker.show();
    }

    private void setFilterSelected(TextView selected, TextView other) {
        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        other.setBackgroundResource(R.drawable.bg_segment_unselected);
        selected.setTextColor(getColor(R.color.nav_icon_active));
        other.setTextColor(getColor(R.color.nav_icon_default));
    }

    private void showCategoryBottomSheet(long userId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_category, null);
        dialog.setContentView(view);
        RecyclerView rv = view.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        db.categoryDao().getCategoriesByUserAndType(userId, isExpense ? "Expense" : "Income")
                .observe(this, list -> {
                    CategoryAdapter adapter = new CategoryAdapter(list, cat -> {
                        tvCategory.setText(cat.name);
                        int resId = getResources().getIdentifier(cat.icon, "drawable", getPackageName());
                        ivCategoryIcon.setImageResource(resId);
                        selectedCategoryId = cat.category_id;
                        dialog.dismiss();
                    });
                    rv.setAdapter(adapter);
                });
        dialog.show();
    }

    private void setupSubtypePicker() {
        SubtypePickerUtil.showSubtypePicker(
                this,
                ivSubtypeIcon,
                tvSubtype,
                "Select Payment Method",   // ✅ dynamic title
                subtype -> {
                    selectedSubtypeId = subtype.subtype_id;
                }
        );
    }


    private void showFrequencyBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_frequency, null);
        dialog.setContentView(view);
        view.findViewById(R.id.tvDaily).setOnClickListener(v -> setFrequency(dialog, "daily"));
        view.findViewById(R.id.tvWeekly).setOnClickListener(v -> setFrequency(dialog, "weekly"));
        view.findViewById(R.id.tvMonthly).setOnClickListener(v -> setFrequency(dialog, "monthly"));
        view.findViewById(R.id.tvYearly).setOnClickListener(v -> setFrequency(dialog, "yearly"));
        dialog.show();
    }

    private void setFrequency(BottomSheetDialog dialog, String freq) {
        selectedFrequency = freq;
        tvFrequency.setText(freq.substring(0, 1).toUpperCase() + freq.substring(1));
        dialog.dismiss();
        calculateEndDate();
    }

    private void calculateEndDate() {
        try {
            if (switchRepeatForever.isChecked() || selectedFrequency == null) {
                tvEndDate.setVisibility(View.GONE);
                return;
            }
            String countStr = etPaymentCount.getText().toString();
            if (countStr.isEmpty()) {
                tvEndDate.setText("End Date: —");
                return;
            }
            int count = Integer.parseInt(countStr);
            Date end = addFrequency(selectedStartDate, selectedFrequency, count);
            tvEndDate.setVisibility(View.VISIBLE);
            tvEndDate.setText(sdf.format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date addFrequency(Date start, String freq, int count) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        switch (freq) {
            case "daily":
                cal.add(Calendar.DAY_OF_MONTH, count);
                break;
            case "weekly":
                cal.add(Calendar.WEEK_OF_YEAR, count);
                break;
            case "monthly":
                cal.add(Calendar.MONTH, count);
                break;
            case "yearly":
                cal.add(Calendar.YEAR, count);
                break;
        }
        return cal.getTime();
    }

    // 🧾 Save Recurring Payment + Generate Schedules
//    private void saveOrUpdateRecurringPayment() {
//        String amountStr = etAmount.getText().toString().trim();
//        String titleStr = etTitle.getText().toString().trim();
//        String notesStr = etNotes.getText().toString().trim();
//
//        // 🔍 UI-Level Validation
//        if (amountStr.isEmpty() || Double.parseDouble(amountStr) <= 0) {
//            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (titleStr.isEmpty()) {
//            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (selectedCategoryId == -1) {
//            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (selectedSubtypeId == -1) {
//            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (selectedFrequency == null) {
//            Toast.makeText(this, "Select payment frequency", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        boolean repeatForever = switchRepeatForever.isChecked();
//
//        // ✅ Use a temporary variable first
//        int tempTotalPayments = 0;
//        try {
//            tempTotalPayments = repeatForever ? 12 : Integer.parseInt(etPaymentCount.getText().toString().trim());
//            if (tempTotalPayments <= 0 && !repeatForever) {
//                Toast.makeText(this, "Total payments must be greater than zero", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        } catch (NumberFormatException e) {
//            if (!repeatForever) {
//                Toast.makeText(this, "Enter valid number of payments", Toast.LENGTH_SHORT).show();
//                return;
//            } else {
//                tempTotalPayments = 12; // fallback default
//            }
//        }
//
//        // ✅ Assign once here as final (now it’s effectively final for lambda use)
//        final int totalPayments = tempTotalPayments;
//
//        double amount = Double.parseDouble(amountStr);
//
//        RecurringTransaction rt = new RecurringTransaction();
//        rt.user_id = 1;
//        rt.category_id = selectedCategoryId;
//        rt.subtype_id = selectedSubtypeId;
//        rt.title = titleStr;
//        rt.amount = amount;
//        rt.type = isExpense ? "Expense" : "Income";
//        rt.frequency = selectedFrequency;
//        rt.repeat_forever = repeatForever;
//        rt.total_payments = totalPayments;
//        rt.completed_payments = 0;
//        rt.start_date = selectedStartDate;
//        rt.next_due_date = addFrequency(selectedStartDate, selectedFrequency, 1);
//        rt.end_date = repeatForever ? null : addFrequency(selectedStartDate, selectedFrequency, totalPayments);
//        rt.status = "active";
//        rt.notes = notesStr;
//        rt.created_at = new Date();
//        rt.updated_at = new Date();
//
//        // 🧠 Field-level Validation before saving
//        if (!validateRecurringTransaction(rt)) {
//            return; // Stop if validation fails
//        }
//
//        // 💾 Insert and schedule logic
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                long newId = db.recurringTransactionDao().insert(rt);
//                rt.recurring_id = newId;
//
//                long transactionId = -1;
//
//                // 🕓 Only create the first transaction if start_date == today
//                Calendar today = Calendar.getInstance();
//                Calendar startCal = Calendar.getInstance();
//                startCal.setTime(rt.start_date);
//
//                boolean isToday = today.get(Calendar.YEAR) == startCal.get(Calendar.YEAR)
//                        && today.get(Calendar.DAY_OF_YEAR) == startCal.get(Calendar.DAY_OF_YEAR);
//
//                if (isToday) {
//                    Transaction t = new Transaction();
//                    t.user_id = rt.user_id;
//                    t.subtype_id = rt.subtype_id;
//                    t.category_id = rt.category_id;
//                    t.recurring_id = rt.recurring_id;
//                    t.amount = rt.amount;
//                    t.type = rt.type;
//
//                    String progressNote;
//                    if (rt.repeat_forever) {
//                        progressNote = rt.title + " recurring payment will continue as scheduled.";
//                    } else {
//                        int completed = rt.completed_payments + 1;
//                        int remaining = rt.total_payments - completed;
//                        if (remaining < 0) remaining = 0;
//
//                        if (remaining == 0) {
//                            progressNote = "All payments for " + rt.title + " are completed successfully.";
//                        } else {
//                            progressNote = rt.title + ": Payment " + completed + " of " + rt.total_payments +
//                                    " completed. " + remaining + " payment" +
//                                    (remaining == 1 ? " remains." : "s remain.");
//                        }
//                    }
//
//                    t.notes = progressNote;
//                    t.date = rt.start_date;
//                    t.created_at = new Date();
//                    t.updated_at = new Date();
//                    transactionId = db.transactionDao().insert(t);
//
//                    rt.completed_payments = 1;
//                    db.recurringTransactionDao().update(rt);
//                }
//
//                // ✅ Generate recurring schedules safely
//                for (int i = 0; i < totalPayments; i++) {
//                    Date due = addFrequency(selectedStartDate, selectedFrequency, i);
//                    RecurringTransactionSchedule schedule = new RecurringTransactionSchedule(
//                            rt.recurring_id,
//                            due,
//                            rt.amount,
//                            (isToday && i == 0) ? "done" : "upcoming"
//                    );
//
//                    // 🧠 Prevent foreign key crash if transactionId is invalid
//                    if (i == 0 && transactionId > 0) {
//                        schedule.transaction_id = transactionId;
//                    } else {
//                        schedule.transaction_id = null;
//                    }
//
//                    try {
//                        db.recurringScheduleDao().insert(schedule);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                runOnUiThread(() -> {
//                    ReviewUtils.showInAppReview(this);
//                    Toast.makeText(this, "Recurring payment created successfully!", Toast.LENGTH_SHORT).show();
//                    finish();
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
//            }
//        });
//    }

    // 🧾 Save Recurring Payment + Generate Schedules
    private void saveOrUpdateRecurringPayment() {
        String amountStr = etAmount.getText().toString().trim();
        String titleStr = etTitle.getText().toString().trim();
        String notesStr = etNotes.getText().toString().trim();

        // 🔍 UI-Level Validation
        if (amountStr.isEmpty() || Double.parseDouble(amountStr) <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (titleStr.isEmpty()) {
            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSubtypeId == -1) {
            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedFrequency == null) {
            Toast.makeText(this, "Select payment frequency", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean repeatForever = switchRepeatForever.isChecked();

        int tempTotalPayments = 0;
        try {
            if (repeatForever) {
                tempTotalPayments = 0; // 🔁 0 means unlimited payments
            } else {
                tempTotalPayments = Integer.parseInt(etPaymentCount.getText().toString().trim());
                if (tempTotalPayments <= 0) {
                    Toast.makeText(this, "Total payments must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            if (!repeatForever) {
                Toast.makeText(this, "Enter a valid number of payments", Toast.LENGTH_SHORT).show();
                return;
            } else {
                tempTotalPayments = 0; // fallback for infinite repeat
            }
        }

        final int totalPayments = tempTotalPayments;
        double amount = Double.parseDouble(amountStr);

        RecurringTransaction rt = new RecurringTransaction();
        rt.user_id = 1;
        rt.category_id = selectedCategoryId;
        rt.subtype_id = selectedSubtypeId;
        rt.title = titleStr;
        rt.amount = amount;
        rt.type = isExpense ? "Expense" : "Income";
        rt.frequency = selectedFrequency;
        rt.repeat_forever = repeatForever;
        rt.total_payments = totalPayments; // 0 = infinite
        rt.completed_payments = 0;
        rt.start_date = selectedStartDate;
//        rt.next_due_date = addFrequency(selectedStartDate, selectedFrequency, 1);
        rt.next_due_date = null;
        rt.end_date = repeatForever ? null : addFrequency(selectedStartDate, selectedFrequency, totalPayments);
        rt.status = "active";
        rt.notes = notesStr;
        rt.created_at = new Date();
        rt.updated_at = new Date();


        if (!validateRecurringTransaction(rt)) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long newId = db.recurringTransactionDao().insert(rt);
                rt.recurring_id = newId;

                long transactionId = -1;

                Calendar today = Calendar.getInstance();
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(rt.start_date);

                boolean isToday = today.get(Calendar.YEAR) == startCal.get(Calendar.YEAR)
                        && today.get(Calendar.DAY_OF_YEAR) == startCal.get(Calendar.DAY_OF_YEAR);

                Date nextDueDate; // 👈 track next due date

                if (isToday) {
                    // 🧾 Create first transaction
                    Transaction t = new Transaction();
                    t.user_id = rt.user_id;
                    t.subtype_id = rt.subtype_id;
                    t.category_id = rt.category_id;
                    t.recurring_id = rt.recurring_id;
                    t.amount = rt.amount;
                    t.type = rt.type;

                    String progressNote;
                    if (rt.repeat_forever) {
                        progressNote = rt.title + " recurring payment will continue as scheduled.";
                    } else {
                        int completed = rt.completed_payments + 1;
                        int remaining = rt.total_payments - completed;
                        if (remaining < 0) remaining = 0;

                        if (remaining == 0) {
                            progressNote = "All payments for " + rt.title + " are completed successfully.";
                        } else {
                            progressNote = rt.title + ": Payment " + completed + " of " + rt.total_payments +
                                    " completed. " + remaining + " payment" +
                                    (remaining == 1 ? " remains." : "s remain.");
                        }
                    }

                    t.notes = progressNote;
                    t.date = rt.start_date;
                    t.created_at = new Date();
                    t.updated_at = new Date();
                    transactionId = db.transactionDao().insert(t);

                    // 🔁 Update recurring transaction progress
                    rt.completed_payments = 1;
                    rt.updated_at = new Date();

                    // ✅ Create "done" schedule
                    RecurringTransactionSchedule firstSchedule = new RecurringTransactionSchedule(
                            rt.recurring_id,
                            rt.start_date,
                            rt.amount,
                            "completed"
                    );
                    if (transactionId > 0) firstSchedule.transaction_id = transactionId;
                    db.recurringScheduleDao().insert(firstSchedule);

                    // ✅ Create upcoming schedule
                    nextDueDate = addFrequency(rt.start_date, rt.frequency, 1);
                    RecurringTransactionSchedule nextSchedule = new RecurringTransactionSchedule(
                            rt.recurring_id,
                            nextDueDate,
                            rt.amount,
                            "upcoming"
                    );
                    nextSchedule.transaction_id = null;
                    db.recurringScheduleDao().insert(nextSchedule);

                } else {
                    // 🗓️ Start date is future → create only upcoming schedule (same date)
                    nextDueDate = rt.start_date;
                    RecurringTransactionSchedule nextSchedule = new RecurringTransactionSchedule(
                            rt.recurring_id,
                            nextDueDate,
                            rt.amount,
                            "upcoming"
                    );
                    nextSchedule.transaction_id = null;
                    db.recurringScheduleDao().insert(nextSchedule);
                }

                // 🕒 Update next_due_date in parent RecurringTransaction
                rt.next_due_date = nextDueDate;
                db.recurringTransactionDao().update(rt);

                // ✅ UI feedback
                runOnUiThread(() -> {
                    ReviewUtils.showInAppReview(this);
                    Toast.makeText(this, "Recurring payment created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

    }


    /**
     * ✅ Field-level validation for RecurringTransaction before DB insert.
     */
    private boolean validateRecurringTransaction(RecurringTransaction rt) {
        if (rt.user_id <= 0) {
            runOnUiThread(() -> Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.category_id <= 0) {
            runOnUiThread(() -> Toast.makeText(this, "Invalid category.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.subtype_id <= 0) {
            runOnUiThread(() -> Toast.makeText(this, "Invalid payment method.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.title == null || rt.title.trim().isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Title is required.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.amount <= 0) {
            runOnUiThread(() -> Toast.makeText(this, "Amount must be greater than zero.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.frequency == null || rt.frequency.trim().isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Frequency not set.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.start_date == null) {
            runOnUiThread(() -> Toast.makeText(this, "Start date is required.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (!rt.repeat_forever && rt.total_payments <= 0) {
            runOnUiThread(() -> Toast.makeText(this, "Total payments must be greater than zero.", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (rt.status == null || rt.status.trim().isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Status is missing.", Toast.LENGTH_SHORT).show());
            return false;
        }
        return true;
    }


    private void loadRecurringPayment(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            RecurringTransaction rt = db.recurringTransactionDao().getByIdSync(id);
            if (rt != null) {
                runOnUiThread(() -> {
                    etAmount.setText(String.valueOf(rt.amount));
                    etTitle.setText(rt.title);
                    etNotes.setText(rt.notes);
                    tvFrequency.setText(rt.frequency);
                    selectedFrequency = rt.frequency;
                    selectedStartDate = rt.start_date;
                    tvStartDate.setText("Start Date: " + sdf.format(rt.start_date));
                    tvEndDate.setText(rt.end_date == null ? "End Date: None" : "End Date: " + sdf.format(rt.end_date));
                    switchRepeatForever.setChecked(rt.repeat_forever);
                    isExpense = "Expense".equalsIgnoreCase(rt.type);
                    setFilterSelected(isExpense ? tvSpend : tvIncome, isExpense ? tvIncome : tvSpend);
                    calculateEndDate();
                });
            }
        });
    }
}
