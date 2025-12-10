package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.dao.SubtypeDao;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Account_Subtype.SubtypePickerUtil;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter.CategoryAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.CategoryUtil;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;
import trackmyspend.budgetplanner.expensemanager.Util.ReviewUtils;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView tvSpend, tvIncome, tvTransfer, tvDateTime, tvCategory, tvPayeeLabel,
            tvAmountTitle, tvSubtype, tvSubtypeTo;
    private TextView currencySymbol;
    private String currentType = "Expense"; // default
    TextView remainingTrans;

    private EditText etPayee, etAmount, etNotes;
    private ImageView ivCalendar, ivCategoryIconTransaction, ivSubtypeIcon, ivSubtypeIconTo;
    private LinearLayout layoutCategory, layoutSubtype, layoutSubtypeTo, linear_paidTo, date_linearlayout;
    private LinearLayout btnDelete;

    private boolean isExpense = true;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
    private AppDatabase db;

    // Stored values
    private long transactionId = -1;
    private long selectedCategoryId = -1;
    private long selectedSubtypeId = -1;
    private long selectedSubtypeToId = -1;
    private boolean isCategorySheetOpenedOnce = false;
    private boolean isSubtypeSheetOpenedOnce = false;
    private boolean isCategoryOpenedAfterSwitch = false;
    User user;


    private ActivityResultLauncher<Intent> addCategoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        db = AppDatabase.getDatabase(this);
        user = db.userDao().getFirstUser();

        // Init UI
        remainingTrans = findViewById(R.id.remainingTrans);
        refreshRemainingTrans();

        layoutCategory = findViewById(R.id.layoutCategory);
        tvCategory = findViewById(R.id.tvCategory);
        ivCategoryIconTransaction = findViewById(R.id.ivCategoryIconTransaction);

        layoutSubtype = findViewById(R.id.layoutSubtype);
        layoutSubtypeTo = findViewById(R.id.layoutSubtypeTo);
        ivSubtypeIcon = findViewById(R.id.ivSubtypeIcon);
        ivSubtypeIconTo = findViewById(R.id.ivSubtypeIconTo);
        tvSubtype = findViewById(R.id.tvSubtype);
        tvSubtypeTo = findViewById(R.id.tvSubtypeTo);

        currencySymbol = findViewById(R.id.currencySymbol);
        tvAmountTitle = findViewById(R.id.tvAmountTitle);
        tvSpend = findViewById(R.id.tvSpend);
        tvIncome = findViewById(R.id.tvIncome);
        tvTransfer = findViewById(R.id.tvTransfer);
        tvPayeeLabel = findViewById(R.id.tvPayeeLabel);
        etPayee = findViewById(R.id.etPayee);
        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);

        date_linearlayout = findViewById(R.id.date_linearlayout);
        tvDateTime = findViewById(R.id.tvDateTime);
        ivCalendar = findViewById(R.id.ivCalendar);

        linear_paidTo = findViewById(R.id.linear_paidTo);


//        btnDelete = findViewById(R.id.btnDelete);

        ImageView ivBack = findViewById(R.id.ivBack);

        FrameLayout bannerContainer = findViewById(R.id.banner_container);
        AdsManager.loadBanner(this, bannerContainer);

        currencySymbol.setText(CurrencyFormatterUtil.getCurrencySymbol());

        // ✅ When user presses Done on Amount field → move to Payee (keep keyboard open)
        // ✅ When user presses Done on Amount field
        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                // ✅ If Transfer → open "From Account" directly
                if ("Transfer".equals(currentType)) {

                    if (transactionId == -1 && !isSubtypeSheetOpenedOnce) {
                        isSubtypeSheetOpenedOnce = true;

                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        v.postDelayed(() -> {
                            // Open FROM account picker
                            setupSubtypePicker();
                        }, 150);
                    }

                    return true;
                }

                // ✅ For Expense/Income → go to Payee
                etPayee.requestFocus();
                return true;
            }
            return false;
        });


// ✅ When user presses Done on Payee field → close keyboard + open Category (only once)
        // ✅ When user presses Done on Payee field → open Category for Expense/Income
        etPayee.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                // ✅ Ignore this logic in Transfer mode (because no Payee exists)
                if ("Transfer".equals(currentType)) return true;

                // ✅ CLOSE keyboard
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // ✅ Scroll smooth
                v.postDelayed(() -> {
                    View scrollView = findViewById(R.id.scrollView);
                    if (scrollView instanceof ScrollView) {
                        ((ScrollView) scrollView).smoothScrollTo(0, 0);
                    }
                }, 100);

                // ✅ For new transactions only open category once
                if (transactionId == -1 && !isCategorySheetOpenedOnce) {
                    isCategorySheetOpenedOnce = true;
                    v.postDelayed(() -> showCategoryBottomSheet(1), 150);
                }

                return true;
            }
            return false;
        });


        // ✅ Register generic launcher
        addCategoryLauncher = CategoryUtil.registerAddCategoryLauncher(this, () -> {
            // Refresh bottom sheet after adding new category
            showCategoryBottomSheet(1);
        });


        ivBack.setOnClickListener(v -> onBackPressed());

        // Default → Spend
        setFilterSelected(tvSpend);
        isExpense = true;
        updatePayeeLabel();

        // Clicks
        layoutCategory.setOnClickListener(v -> showCategoryBottomSheet(1));
        layoutSubtype.setOnClickListener(v -> setupSubtypePicker());

        layoutSubtypeTo.setOnClickListener(v -> setupSubtypePickerTo());

        tvSpend.setOnClickListener(v -> {
            clearForm();
            setFilterSelected(tvSpend);
            currentType = "Expense";
            updatePayeeLabel();
            resetFlowFlags();
        });

        tvIncome.setOnClickListener(v -> {
            clearForm();
            setFilterSelected(tvIncome);
            currentType = "Income";
            updatePayeeLabel();
            resetFlowFlags();
        });

        tvTransfer.setOnClickListener(v -> {
            clearForm();
            setFilterSelected(tvTransfer);
            currentType = "Transfer";
            updatePayeeLabel();
            resetFlowFlags();
        });


        tvDateTime.setText(sdf.format(calendar.getTime()));
        setupDateTimePicker(tvDateTime, date_linearlayout);

        // Save
        findViewById(R.id.btnSave).setOnClickListener(v -> {

            if (user != null && user.remaining_transaction_cnt > 0) {
                db.userDao().addRemainingTransactions(user.user_id, -1);

                if (transactionId == -1) {
                    saveTransaction();
                } else {
                    updateTransaction();
                }

            } else {
                Toast.makeText(this, "No remaining transactions", Toast.LENGTH_SHORT).show();
            }


        });

        // ✅ Check edit mode
        transactionId = getIntent().getLongExtra("transaction_id", -1);
        if (transactionId != -1) {
            loadTransaction(transactionId);
            tvTransfer.setVisibility(View.GONE);
        }
    }

    private void refreshRemainingTrans() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final int pts = (user != null) ? user.remaining_transaction_cnt : 0;
                runOnUiThread(() -> remainingTrans.setText(pts + " pts"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> remainingTrans.setText("0 pts"));
            }
        });
    }

    private void resetFlowFlags() {
        isCategorySheetOpenedOnce = false;
        isSubtypeSheetOpenedOnce = false;
        isCategoryOpenedAfterSwitch = false;
    }


    private void setFilterSelected(TextView selected) {
        int activeColor = getResources().getColor(R.color.nav_icon_active, getTheme());
        int defaultColor = getResources().getColor(R.color.nav_icon_default, getTheme());

        tvSpend.setBackgroundResource(R.drawable.bg_segment_unselected);
        tvIncome.setBackgroundResource(R.drawable.bg_segment_unselected);
        tvTransfer.setBackgroundResource(R.drawable.bg_segment_unselected);

        tvSpend.setTextColor(defaultColor);
        tvIncome.setTextColor(defaultColor);
        tvTransfer.setTextColor(defaultColor);

        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(activeColor);
    }


    private void setupDateTimePicker(TextView targetView, LinearLayout date_linearlayout) {
        View.OnClickListener openPicker = v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    AddTransactionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePicker = new TimePickerDialog(
                                AddTransactionActivity.this,
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    targetView.setText(sdf.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                        );
                        timePicker.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        };

        targetView.setOnClickListener(openPicker);
        if (date_linearlayout != null) date_linearlayout.setOnClickListener(openPicker);
    }

    private void setupSubtypePicker() {
        String titleText;

        // ✅ Decide title based on type
        if ("Transfer".equalsIgnoreCase(currentType)) {
            titleText = "Withdrawn From";
        } else {
            titleText = "Select Payment Method";
        }

        SubtypePickerUtil.showSubtypePicker(
                this,
                ivSubtypeIcon,
                tvSubtype,
                titleText,
                subtype -> {
                    selectedSubtypeId = subtype.subtype_id;

                    // ✅ Only auto-open "To Account" if Transfer mode
                    if ("Transfer".equalsIgnoreCase(currentType)) {
                        tvSubtype.postDelayed(this::setupSubtypePickerTo, 150);
                    }
                }
        );
    }


    private void setupSubtypePickerTo() {
        SubtypePickerUtil.showSubtypePicker(
                this,
                ivSubtypeIconTo,
                tvSubtypeTo,
                "Deposited To",
                subtype -> selectedSubtypeToId = subtype.subtype_id
        );
    }


    private void showCategoryBottomSheet(long userId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_category, null);
        dialog.setContentView(sheetView);

        ImageView closeBottomSheet = sheetView.findViewById(R.id.closeBottomSheet);
        closeBottomSheet.setOnClickListener(v -> {
            dialog.dismiss();
        });

        LinearLayout btnAddNewCategory = sheetView.findViewById(R.id.btnAddCategory);
        RecyclerView rvCategories = sheetView.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));

        db.categoryDao().getCategoriesByUserAndType(userId, currentType)
                .observe(this, categories -> {
                    CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                        int resId = getResources().getIdentifier(category.icon, "drawable", getPackageName());
                        ivCategoryIconTransaction.setImageResource(resId);
                        tvCategory.setText(category.name);
                        selectedCategoryId = category.category_id;
                        dialog.dismiss();

                        // ✅ Open Subtype Picker automatically only once for new transactions
                        // ✅ After category selection → open Subtype Picker automatically only once
                        if (transactionId == -1 && !isSubtypeSheetOpenedOnce) {
                            isSubtypeSheetOpenedOnce = true;
                            tvCategory.postDelayed(() -> setupSubtypePicker(), 150);
                        }
                    });
                    rvCategories.setAdapter(adapter);
                });

        // ✅ Use generic util for adding category
        btnAddNewCategory.setOnClickListener(v -> {
            CategoryUtil.openAddCategory(this, isExpense, addCategoryLauncher);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveTransaction() {

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("save_transaction", null);

        String amountStr = etAmount.getText().toString().trim();

        // ✅ Validate Amount first
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // ✅ Transfer Logic
        if (currentType.equals("Transfer")) {

            if (selectedSubtypeId == -1 || selectedSubtypeToId == -1) {
                Toast.makeText(this, "Select From and To Accounts", Toast.LENGTH_SHORT).show();
                return;
            }

            String notesInput = etNotes.getText().toString().trim();

            Executors.newSingleThreadExecutor().execute(() -> {
                // ✅ Fetch category_id for "Transfer"
                long transferCategoryId = db.categoryDao().getCategoryIdByName("Transfer");

                // ✅ Get both account names using subtype
                SubtypeDao.AccountInfo fromAccount = db.subtypeDao().getAccountInfoBySubtypeId(selectedSubtypeId);
                SubtypeDao.AccountInfo toAccount = db.subtypeDao().getAccountInfoBySubtypeId(selectedSubtypeToId);

                String fromAccountName = fromAccount != null ? fromAccount.account_name : "From Account";
                String toAccountName = toAccount != null ? toAccount.account_name : "To Account";

                String transferNote = notesInput.isEmpty()
                        ? "Transfer from " + fromAccountName + " to " + toAccountName
                        : notesInput + " | Transfer from " + fromAccountName + " to " + toAccountName;
                long groupId = System.currentTimeMillis(); // unique

                // 🔻 Debit (From Account)
                Transaction debit = new Transaction();
                debit.user_id = 1;
                debit.amount = amount;
                debit.source_name = "Debit from " + fromAccountName;
                debit.notes = transferNote;
                debit.date = calendar.getTime();
                debit.type = "TransferDebit";
                debit.transfer_group_id = groupId;
                debit.category_id = transferCategoryId;
                debit.subtype_id = selectedSubtypeId;
                debit.created_at = debit.updated_at = new Date();

                // 🔺 Credit (To Account)
                Transaction credit = new Transaction();
                credit.user_id = 1;
                credit.amount = amount;
                credit.source_name = "Credit to " + toAccountName;
                credit.notes = transferNote;
                credit.date = calendar.getTime();
                credit.type = "TransferCredit";
                credit.transfer_group_id = groupId;
                credit.category_id = transferCategoryId;
                credit.subtype_id = selectedSubtypeToId;
                credit.created_at = credit.updated_at = new Date();

                // ✅ Insert both transactions
                db.transactionDao().insert(debit);
                db.transactionDao().insert(credit);

                runOnUiThread(this::finish);
            });

            ReviewUtils.showInAppReview(this);

            return;
        }


        // ✅ Expense / Income Logic
        String payeeStr = etPayee.getText().toString().trim();

        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSubtypeId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction txn = new Transaction();
        txn.user_id = 1;
        txn.amount = amount;
        txn.source_name = payeeStr;
        txn.notes = etNotes.getText().toString().trim();
        txn.date = calendar.getTime();
        txn.type = currentType; // ✅ No isExpense, directly use type string
        txn.category_id = selectedCategoryId;
        txn.subtype_id = selectedSubtypeId;
        txn.created_at = new Date();
        txn.updated_at = new Date();

        Executors.newSingleThreadExecutor().execute(() -> {
            db.transactionDao().insert(txn);
            runOnUiThread(this::finish);
        });
    }


    private void updateTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String payeeStr = etPayee.getText().toString().trim();

        // ✅ Validate amount
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validate payee
//        if (payeeStr.isEmpty()) {
//            Toast.makeText(this, "Enter payee/source name", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // ✅ Validate category
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Validate subtype
        if (selectedSubtypeId == -1) {
            Toast.makeText(this, "Please select a subtype", Toast.LENGTH_SHORT).show();
            return;
        }

        double newAmount = Double.parseDouble(amountStr);

        Executors.newSingleThreadExecutor().execute(() -> {
            Transaction txn = db.transactionDao().getTransactionByIdSync(transactionId);
            if (txn != null) {
                String oldType = txn.type;
                txn.type = currentType;

                txn.amount = newAmount;
                txn.source_name = etPayee.getText().toString().trim();
                txn.notes = etNotes.getText().toString().trim();
                txn.date = calendar.getTime();
                txn.type = currentType;
                txn.category_id = (selectedCategoryId != -1) ? selectedCategoryId : null;
                txn.subtype_id = (selectedSubtypeId != -1) ? selectedSubtypeId : null;
                txn.updated_at = new Date();
                db.transactionDao().update(txn);

            }
            runOnUiThread(this::finish);
        });
    }


    private void loadTransaction(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Transaction txn = db.transactionDao().getTransactionByIdSync(id);
            if (txn != null) {
                runOnUiThread(() -> {
                    // Fill form
                    etAmount.setText(String.valueOf(txn.amount));
                    etPayee.setText(txn.source_name);
                    etNotes.setText(txn.notes);
                    tvDateTime.setText(sdf.format(txn.date));
                    calendar.setTime(txn.date);

                    currentType = txn.type;

                    if ("Expense".equalsIgnoreCase(txn.type)) setFilterSelected(tvSpend);
                    else if ("Income".equalsIgnoreCase(txn.type)) setFilterSelected(tvIncome);
                    else setFilterSelected(tvTransfer);

                    updatePayeeLabel();


                    // Category
                    if (txn.category_id != null) {
                        selectedCategoryId = txn.category_id;
                        Executors.newSingleThreadExecutor().execute(() -> {
                            Category category = db.categoryDao().getCategoryById(selectedCategoryId);
                            if (category != null) {
                                runOnUiThread(() -> {
                                    ivCategoryIconTransaction.setImageResource(
                                            getResources().getIdentifier(category.icon, "drawable", getPackageName())
                                    );
                                    tvCategory.setText(category.name);
                                });
                            }
                        });
                    }

                    // Subtype
                    if (txn.subtype_id != null) {
                        selectedSubtypeId = txn.subtype_id;
                        Executors.newSingleThreadExecutor().execute(() -> {
                            Subtype subtype = db.subtypeDao().getSubtypeById(selectedSubtypeId);
                            if (subtype != null) {
                                runOnUiThread(() -> {
                                    ivSubtypeIcon.setImageResource(
                                            getResources().getIdentifier(subtype.icon, "drawable", getPackageName())
                                    );
                                    tvSubtype.setText(subtype.name);
                                });
                            }
                        });
                    }

//                    btnDelete.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updatePayeeLabel() {
        switch (currentType) {
            case "Expense":
                tvPayeeLabel.setText("Paid to");
                tvAmountTitle.setText("Amount spent");
                tvSubtype.setText("Payment Method");
                linear_paidTo.setVisibility(View.VISIBLE);
                layoutCategory.setVisibility(View.VISIBLE);
                layoutSubtypeTo.setVisibility(View.GONE);
                break;

            case "Income":
                tvPayeeLabel.setText("Received from");
                tvAmountTitle.setText("Amount received");
                tvSubtype.setText("Payment Method");
                linear_paidTo.setVisibility(View.VISIBLE);
                layoutCategory.setVisibility(View.VISIBLE);
                layoutSubtypeTo.setVisibility(View.GONE);
                break;

            case "Transfer":
                tvSubtype.setText("From Account");
                tvSubtypeTo.setText("To Account");
                tvAmountTitle.setText("Transfer amount");
                linear_paidTo.setVisibility(View.GONE);
                layoutCategory.setVisibility(View.GONE);
                layoutSubtypeTo.setVisibility(View.VISIBLE);

                break;
        }
    }


    private void clearForm() {
        // Reset category
        tvCategory.setText("Category");
        ivCategoryIconTransaction.setImageResource(R.drawable.ic_category);
        selectedCategoryId = -1;

        // Reset subtype
        tvSubtype.setText("Payment Method");
        ivSubtypeIcon.setImageResource(R.drawable.ic_bank);
        selectedSubtypeId = -1;

        // Reset datetime to now
        calendar.setTime(new Date());
        // tvDateTime.setText(sdf.format(calendar.getTime()));

        // ✅ DO NOT RESET type here
        // Keep currentType as is

        // ✅ Only update UI styling and labels based on existing currentType
        if ("Expense".equalsIgnoreCase(currentType)) {
            setFilterSelected(tvSpend);
        } else if ("Income".equalsIgnoreCase(currentType)) {
            setFilterSelected(tvIncome);
        } else {
            setFilterSelected(tvTransfer);
        }

        updatePayeeLabel();
    }


}
