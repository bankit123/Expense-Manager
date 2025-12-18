package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.CategoryIconColorPickerUtil;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.InterstitialAdUtil;
import trackmyspend.budgetplanner.expensemanager.R;

public class Add_Account_Activity extends AppCompatActivity {

    // Account Section
    private EditText etAccountName;
    private RecyclerView rvIcons, rvColors;
    private FrameLayout iconContainer;
    private ImageView ivAccountIcon;
    private TextView tvAccountLetter;

    // Subtype Section
    private EditText etSubtypeName;
    private RecyclerView rvSubtypeIcons, rvSubtypeColors;
    private FrameLayout subtypeIconContainer;
    private ImageView ivSubtypeIcon;
    private TextView tvSubtypeLetter;

    // Custom Dropdown
    private LinearLayout dropdownContainer;
    private TextView tvSelectedFilter;
    private ImageView ivDropdownArrow, ivBack;

    // Shared Save Button
    private LinearLayout btnSave;
    TextView addBtnText;

    // Layout Sections
    private LinearLayout sectionAddAccount, sectionAddSubtypeOnly;
    private TextView tvAddAccount, tvAddPaymentMethod;

    // Database
    private AppDatabase db;
    private List<Account> existingAccounts = new ArrayList<>();
    private String selectedAccountIcon, selectedAccountColor;
    private String selectedSubtypeIcon, selectedSubtypeColor;
    private long selectedAccountId = -1;
    private String selectedAccountName = null;
    private boolean isAddAccount = true; // track mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        FrameLayout bannerContainer = findViewById(R.id.banner_container);
        PriorityBannerController.show(
                this,
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );



        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.main_bg)
        );

        db = AppDatabase.getDatabase(this);
        initViews();
        loadExistingAccounts();
        setupToggleSegment();
        setupPickers();
        setupDropdownSelector();
        setupSaveButton();

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

    }

    /** Initialize all UI components */
//    private void initViews() {
//        // Account section
//        etAccountName = findViewById(R.id.etAccountName);
//        rvIcons = findViewById(R.id.rvIcons);
//        rvColors = findViewById(R.id.rvColors);
//        iconContainer = findViewById(R.id.iconContainer);
//        ivAccountIcon = findViewById(R.id.ivAccountIcon);
//        //tvAccountLetter = findViewById(R.id.tvAccountLetter);
//
//        // Subtype section
//        etSubtypeName = findViewById(R.id.etSubtypeName);
//        rvSubtypeIcons = findViewById(R.id.rvSubtypeIcons);
//        rvSubtypeColors = findViewById(R.id.rvSubtypeColors);
//        subtypeIconContainer = findViewById(R.id.subtypeIconContainer);
//        ivSubtypeIcon = findViewById(R.id.ivSubtypeIcon);
//
//        // Dropdown
//        dropdownContainer = findViewById(R.id.dropdownContainer);
//        tvSelectedFilter = findViewById(R.id.tvSelectedFilter);
//        ivDropdownArrow = findViewById(R.id.ivDropdownArrow);
//
//        // Save button
//        btnSave = findViewById(R.id.btnSave);
//        addBtnText = findViewById(R.id.addBtnText);
//
//        // Sections
//        sectionAddAccount = findViewById(R.id.sectionAddAccount);
//        sectionAddSubtypeOnly = findViewById(R.id.sectionAddSubtypeOnly);
//        tvAddAccount = findViewById(R.id.tvAddAccount);
//        tvAddPaymentMethod = findViewById(R.id.tvAddPaymentMethod);
//
//
//        etAccountName.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
//                v.clearFocus();
//                android.view.inputmethod.InputMethodManager imm =
//                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
//                return true;
//            }
//            return false;
//        });
//
//        etSubtypeName.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
//                v.clearFocus();
//                android.view.inputmethod.InputMethodManager imm =
//                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
//                return true;
//            }
//            return false;
//        });
//
//    }

    private void initViews() {

        // Step 1: Initialize ALL views FIRST
        etAccountName = findViewById(R.id.etAccountName);
        rvIcons = findViewById(R.id.rvIcons);
        rvColors = findViewById(R.id.rvColors);
        iconContainer = findViewById(R.id.iconContainer);
        ivAccountIcon = findViewById(R.id.ivAccountIcon);

        etSubtypeName = findViewById(R.id.etSubtypeName);
        rvSubtypeIcons = findViewById(R.id.rvSubtypeIcons);
        rvSubtypeColors = findViewById(R.id.rvSubtypeColors);
        subtypeIconContainer = findViewById(R.id.subtypeIconContainer);
        ivSubtypeIcon = findViewById(R.id.ivSubtypeIcon);

        dropdownContainer = findViewById(R.id.dropdownContainer);
        tvSelectedFilter = findViewById(R.id.tvSelectedFilter);
        ivDropdownArrow = findViewById(R.id.ivDropdownArrow);

        btnSave = findViewById(R.id.btnSave);
        addBtnText = findViewById(R.id.addBtnText);

        sectionAddAccount = findViewById(R.id.sectionAddAccount);
        sectionAddSubtypeOnly = findViewById(R.id.sectionAddSubtypeOnly);

        tvAddAccount = findViewById(R.id.tvAddAccount);
        tvAddPaymentMethod = findViewById(R.id.tvAddPaymentMethod);

        // Step 2: NOW it's safe to read intent and modify views
        String mode = getIntent().getStringExtra("mode");
        long passedAccountId = getIntent().getLongExtra("accountId", -1);

        if ("add_subtype".equals(mode) && passedAccountId != -1) {

            isAddAccount = false;

            // Switch UI to "Add Payment Method"
            tvAddPaymentMethod.setBackgroundResource(R.drawable.bg_segment_selected);
            tvAddPaymentMethod.setTextColor(ContextCompat.getColor(this, R.color.opposite_color));

            tvAddAccount.setBackgroundResource(R.drawable.bg_segment_unselected);
            tvAddAccount.setTextColor(ContextCompat.getColor(this, R.color.gray));

            sectionAddAccount.setVisibility(View.GONE);
            sectionAddSubtypeOnly.setVisibility(View.VISIBLE);

            // Pre-select dropdown account
            Executors.newSingleThreadExecutor().execute(() -> {
                existingAccounts = db.accountDao().getAllAccounts();

                for (Account acc : existingAccounts) {
                    if (acc.account_id == passedAccountId) {
                        selectedAccountId = acc.account_id;
                        selectedAccountName = acc.name;

                        runOnUiThread(() ->
                                tvSelectedFilter.setText(selectedAccountName)
                        );
                        break;
                    }
                }
            });
        }

        // Step 3: Setup keyboard hide listeners (unchanged)
        etAccountName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                v.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        etSubtypeName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                v.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }


    /** 🔘 Custom Segmented Toggle (Add Account / Add Payment Method) */
    private void setupToggleSegment() {
        View.OnClickListener toggleListener = v -> {
            if (v.getId() == R.id.tvAddAccount) {
                // Highlight Add Account
                isAddAccount = true;
                tvAddAccount.setBackgroundResource(R.drawable.bg_segment_selected);
                tvAddAccount.setTextColor(ContextCompat.getColor(this, R.color.opposite_color));

                tvAddPaymentMethod.setBackgroundResource(R.drawable.bg_segment_unselected);
                tvAddPaymentMethod.setTextColor(ContextCompat.getColor(this, R.color.gray));

                sectionAddAccount.setVisibility(View.VISIBLE);
                sectionAddSubtypeOnly.setVisibility(View.GONE);
//                addBtnText.setText("Save Account");
            } else {
                // Highlight Add Payment Method
                isAddAccount = false;
                tvAddPaymentMethod.setBackgroundResource(R.drawable.bg_segment_selected);
                tvAddPaymentMethod.setTextColor(ContextCompat.getColor(this, R.color.opposite_color));

                tvAddAccount.setBackgroundResource(R.drawable.bg_segment_unselected);
                tvAddAccount.setTextColor(ContextCompat.getColor(this, R.color.gray));

                sectionAddAccount.setVisibility(View.GONE);
                sectionAddSubtypeOnly.setVisibility(View.VISIBLE);
//                addBtnText.setText("Save Payment Method");
            }
        };

        tvAddAccount.setOnClickListener(toggleListener);
        tvAddPaymentMethod.setOnClickListener(toggleListener);
    }

    /** 🔹 Load existing accounts for dropdown */
    private void loadExistingAccounts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingAccounts = db.accountDao().getAllAccounts();
        });
    }

    /** 🔹 Setup icon & color pickers for both Account and Subtype */
    private void setupPickers() {
        CategoryIconColorPickerUtil.setupIconAndColorPicker(
                this, iconContainer, ivAccountIcon, rvIcons, rvColors,
                false, (iconName, drawableName, colorHex) -> {
                    selectedAccountIcon = drawableName;
                    selectedAccountColor = colorHex;
                });

        CategoryIconColorPickerUtil.setupIconAndColorPicker(
                this, subtypeIconContainer, ivSubtypeIcon, rvSubtypeIcons, rvSubtypeColors,
                false, (iconName, drawableName, colorHex) -> {
                    selectedSubtypeIcon = drawableName;
                    selectedSubtypeColor = colorHex;
                });
    }

    /** 🔽 Custom dropdown for selecting existing account */
    private void setupDropdownSelector() {
        dropdownContainer.setOnClickListener(v -> {
            if (existingAccounts == null || existingAccounts.isEmpty()) {
                Toast.makeText(this, "No existing accounts found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Rotate arrow for UX
            ivDropdownArrow.animate().rotation(180).setDuration(150).start();

            List<String> accountNames = new ArrayList<>();
            for (Account a : existingAccounts) accountNames.add(a.name);

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Select Account")
                    .setItems(accountNames.toArray(new String[0]), (dialog, which) -> {
                        selectedAccountName = accountNames.get(which);
                        selectedAccountId = existingAccounts.get(which).account_id;
                        tvSelectedFilter.setText(selectedAccountName);
                        ivDropdownArrow.animate().rotation(0).setDuration(150).start();
                    })
                    .setOnDismissListener(d -> ivDropdownArrow.animate().rotation(0).setDuration(150).start())
                    .show();
        });
    }

    /** 🔹 Unified Save Button */
    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (isAddAccount) {
                saveAccount();
            } else {
                saveSubtype();
            }
        });
    }

    /** ✅ Save new Account */
    private void saveAccount() {
        String name = etAccountName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter account name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAccountIcon == null || selectedAccountColor == null) {
            Toast.makeText(this, "Select icon and color", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Account account = new Account();
            account.user_id = 1;
            account.name = name;
            account.icon = selectedAccountIcon;
            account.iconColorHex = selectedAccountColor;
            account.amount = 0;
            account.created_at = new Date();
            account.updated_at = new Date();

            db.accountDao().insert(account);

            runOnUiThread(() -> {
                Toast.makeText(this, "Account added successfully!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            });
        });
    }

    /** ✅ Save Subtype for selected Account */
    private void saveSubtype() {
        if (selectedAccountId == -1) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etSubtypeName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter subtype name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSubtypeIcon == null || selectedSubtypeColor == null) {
            Toast.makeText(this, "Select icon and color", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Subtype subtype = new Subtype();
            subtype.account_id = selectedAccountId;
            subtype.name = name;
            subtype.icon = selectedSubtypeIcon;
            subtype.backgroundColorHex = selectedSubtypeColor;
            db.subtypeDao().insert(subtype);

            runOnUiThread(() -> {
                Toast.makeText(this, "Payment method added successfully!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            });
        });
    }
}
