package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.bottomsheet.SubtypeBottomSheet;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.DateHeader;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.TransactionGroupedAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.FilterBottomSheetUtil;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.FilterUtil;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

public class Account_Details_Activity extends AppCompatActivity {

    private TextView tvAccountName, tvBalance, tvEmpty, tvSubtitle;
    private TextView tvAll, tvMonthly, tvYearly, tvPeriod;
    private RecyclerView rvTransactions;
    private LinearLayout layoutEmptyState;

    private AppDatabase db;
    private long accountId;
    private String accountName;
    private Date startDate, endDate;

    // Active filters
    private final Set<String> selectedTypes = new HashSet<>(); // Income, Expense, or both
    private final Set<Long> selectedSubtypeIds = new HashSet<>();

    private final SimpleDateFormat rangeFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat dateOnlyFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_details);


        FrameLayout bannerContainer = findViewById(R.id.banner_container);
        PriorityBannerController.show(
                this,
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );

        ImageView ivPaymentMethodBtn = findViewById(R.id.ivPaymentMethodBtn);
        ivPaymentMethodBtn.setOnClickListener(v -> openSubtypeBottomSheet());


        initViews();
        initDatabase();
        initDefaults();
        setupDateFilters();
        setupFilterButton();
    }

    // -------------------------------------------------------
    // ✅ Init
    // -------------------------------------------------------
    private void initViews() {
        tvAccountName = findViewById(R.id.tvAccountName);
        tvBalance = findViewById(R.id.tvBalance);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        rvTransactions = findViewById(R.id.rvTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

//        tvAll = findViewById(R.id.tvAll);
        tvMonthly = findViewById(R.id.tvMonthly);
        tvYearly = findViewById(R.id.tvYearly);
        tvPeriod = findViewById(R.id.tvPeriod);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void initDatabase() {
        db = AppDatabase.getDatabase(this);
        accountId = getIntent().getLongExtra("accountId", -1);
        accountName = getIntent().getStringExtra("accountName");
        if (accountName != null) tvAccountName.setText(accountName);
    }

    private void openSubtypeBottomSheet() {
        SubtypeBottomSheet sheet = SubtypeBottomSheet.newInstance(accountId);
        sheet.show(getSupportFragmentManager(), "SubtypeBottomSheet");
    }


    private void initDefaults() {
        setFilterSelected(tvMonthly, tvYearly, tvPeriod);
        selectedTypes.add("Income");
        selectedTypes.add("Expense");
        selectedTypes.add("TransferCredit");
        selectedTypes.add("TransferDebit");
        FilterUtil.getAllRange((start, end, label) -> {
            startDate = start;
            endDate = end;
            tvSubtitle.setText(label);
            loadFilteredTransactions();
        });
    }

    // -------------------------------------------------------
    // ✅ Date Filters
    // -------------------------------------------------------
    private void setupDateFilters() {
//        tvAll.setOnClickListener(v -> {
//            setFilterSelected(tvAll, tvMonthly, tvYearly, tvPeriod);
//            FilterUtil.getAllRange((start, end, label) -> {
//                startDate = start;
//                endDate = end;
//                tvSubtitle.setText(label);
//                loadFilteredTransactions();
//            });
//        });

        tvMonthly.setOnClickListener(v -> {
            setFilterSelected(tvMonthly, tvYearly, tvPeriod);
            FilterUtil.getMonthlyRange((start, end, label) -> {
                startDate = start;
                endDate = end;
                tvSubtitle.setText(label);
                loadFilteredTransactions();
            });
        });

        tvYearly.setOnClickListener(v -> {
            setFilterSelected(tvYearly, tvMonthly, tvPeriod);
            FilterUtil.getYearlyRange((start, end, label) -> {
                startDate = start;
                endDate = end;
                tvSubtitle.setText(label);
                loadFilteredTransactions();
            });
        });

        tvPeriod.setOnClickListener(v -> {
            setFilterSelected(tvPeriod, tvMonthly, tvYearly);
            FilterUtil.showPeriodPicker(this, getSupportFragmentManager(),
                    (start, end, label) -> {
                        startDate = start;
                        endDate = end;
                        tvSubtitle.setText("Period: " + label);
                        loadFilteredTransactions();
                    });
        });
    }

    // -------------------------------------------------------
    // ✅ Filter Bottom Sheet (Reusable Utility)
    // -------------------------------------------------------
    private void setupFilterButton() {
        LinearLayout filterContainer = findViewById(R.id.filterContainer);
        filterContainer.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        FilterBottomSheetUtil.show(
                this,
                accountId,
                selectedTypes,
                selectedSubtypeIds,
                new FilterBottomSheetUtil.OnFilterAppliedListener() {
                    @Override
                    public void onApply(Set<String> types, Set<Long> subtypes) {
                        // Re-apply filters and reload
                        loadFilteredTransactions();
                    }

                    @Override
                    public void onClear() {
                        selectedTypes.clear();
                        selectedTypes.add("Income");
                        selectedTypes.add("Expense");
                        selectedTypes.add("TransferCredit");
                        selectedTypes.add("TransferDebit");
                        selectedSubtypeIds.clear();
                        loadFilteredTransactions();
                    }
                }
        );
    }

    // -------------------------------------------------------
    // ✅ Load Grouped Transactions
    // -------------------------------------------------------
    @SuppressLint("SetTextI18n")
    private void loadFilteredTransactions() {
        db.transactionDao().getFilteredTransactions(accountId, "All", -1, startDate, endDate)
                .observe(this, transactions -> {
                    if (transactions == null || transactions.isEmpty()) {
                        rvTransactions.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        tvBalance.setText("Balance: ₹0.00");
                        return;
                    }

                    // Filter by Type and Subtype
                    List<Transaction> filtered = new ArrayList<>();
                    for (Transaction t : transactions) {
                        boolean matchesType = selectedTypes.contains(t.type);
                        boolean matchesSubtype = selectedSubtypeIds.isEmpty() ||
                                selectedSubtypeIds.contains(t.subtype_id);
                        if (matchesType && matchesSubtype) filtered.add(t);
                    }

                    if (filtered.isEmpty()) {
                        rvTransactions.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        tvBalance.setText("Balance: ₹0.00");
                        return;
                    }

                    rvTransactions.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);

                    // ✅ Corrected Balance Calculation
                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (Transaction t : filtered) {

                        if ("Income".equalsIgnoreCase(t.type)) {
                            totalIncome += t.amount;
                        }
                        else if ("Expense".equalsIgnoreCase(t.type)) {
                            totalExpense += t.amount;
                        }
                        else if ("TransferCredit".equalsIgnoreCase(t.type)) {
                            // ✅ Money coming IN to this account
                            totalIncome += t.amount;
                        }
                        else if ("TransferDebit".equalsIgnoreCase(t.type)) {
                            // ✅ Money going OUT from this account
                            totalExpense += t.amount;
                        }
                    }

                    double balance = totalIncome - totalExpense;
                    tvBalance.setText("Balance: " + CurrencyFormatterUtil.format(balance));

                    // ✅ Grouped transactions
                    List<Object> groupedList = groupTransactionsByDate(filtered);
                    rvTransactions.setAdapter(new TransactionGroupedAdapter(this, groupedList, db));
                });
    }

    // -------------------------------------------------------
    // ✅ Group by Date
    // -------------------------------------------------------
//    private List<Object> groupTransactionsByDate(List<Transaction> transactions) {
//        Map<String, List<Transaction>> groupedMap = new LinkedHashMap<>();
//
//        for (Transaction t : transactions) {
//            String dateKey = dateOnlyFormat.format(t.date);
//            groupedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(t);
//        }
//
//        List<Object> groupedItems = new ArrayList<>();
//
//        for (Map.Entry<String, List<Transaction>> entry : groupedMap.entrySet()) {
//            List<Transaction> dailyList = entry.getValue();
//            double incomeTotal = 0, expenseTotal = 0;
//
//            for (Transaction t : dailyList) {
//
//                if ("Income".equalsIgnoreCase(t.type)) {
//                    incomeTotal += t.amount;
//
//                } else if ("Expense".equalsIgnoreCase(t.type)) {
//                    expenseTotal += t.amount;
//
//                } else if ("TransferCredit".equalsIgnoreCase(t.type)) {
//                    // ✅ Treat transfer credit like income
//                    incomeTotal += t.amount;
//
//                } else if ("TransferDebit".equalsIgnoreCase(t.type)) {
//                    // ✅ Treat transfer debit like expense
//                    expenseTotal += t.amount;
//                }
//            }
//
//            DateHeader header;
//            try {
//                header = new DateHeader(dateOnlyFormat.parse(entry.getKey()), incomeTotal, expenseTotal);
//            } catch (Exception e) {
//                header = new DateHeader(new Date(), incomeTotal, expenseTotal);
//            }
//
//            groupedItems.add(header);
//            groupedItems.addAll(dailyList);
//        }
//
//        return groupedItems;
//    }

    private List<Object> groupTransactionsByDate(List<Transaction> transactions) {

        int adFrequency = 0;
        try {
            String cnt =
                    trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager
                            .getConfig()
                            .get("b_home_trans_ads_cnt");

            adFrequency = Integer.parseInt(cnt);
        } catch (Exception ignored) {
            adFrequency = 0;
        }

        Map<String, List<Transaction>> groupedMap = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String dateKey = dateOnlyFormat.format(t.date);
            groupedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(t);
        }

        List<Object> groupedItems = new ArrayList<>();

        int txnCounter = 0;  // ✅ Count transactions instead of headers

        for (Map.Entry<String, List<Transaction>> entry : groupedMap.entrySet()) {

            List<Transaction> dailyList = entry.getValue();
            double incomeTotal = 0, expenseTotal = 0;

            for (Transaction t : dailyList) {
                if ("Income".equalsIgnoreCase(t.type)) {
                    incomeTotal += t.amount;
                } else if ("Expense".equalsIgnoreCase(t.type)) {
                    expenseTotal += t.amount;
                } else if ("TransferCredit".equalsIgnoreCase(t.type)) {
                    incomeTotal += t.amount;
                } else if ("TransferDebit".equalsIgnoreCase(t.type)) {
                    expenseTotal += t.amount;
                }
            }

            // ✅ REMOVE old headerCounter logic
            // ❌ DO NOT insert ad before header now

            DateHeader header;
            try {
                header = new DateHeader(dateOnlyFormat.parse(entry.getKey()), incomeTotal, expenseTotal);
            } catch (Exception e) {
                header = new DateHeader(new Date(), incomeTotal, expenseTotal);
            }

            groupedItems.add(header);

            // ✅ Add transactions one by one and insert ADS after every adFrequency transactions
            for (Transaction t : dailyList) {
                groupedItems.add(t);
                txnCounter++;

                if (adFrequency > 0 && txnCounter % adFrequency == 0) {
                    groupedItems.add("AD_PLACEHOLDER");
                }
            }
        }

        return groupedItems;
    }


//    private List<Object> groupTransactionsByDate(List<Transaction> transactions) {
//
//        int adFrequency = 0;
//        try {
//            adFrequency = AdsManager.getAccDetailsBannerAdFrequency();
//        } catch (Exception ignored) {
//            adFrequency = 0;
//        }
//
//        Map<String, List<Transaction>> groupedMap = new LinkedHashMap<>();
//
//        for (Transaction t : transactions) {
//            String dateKey = dateOnlyFormat.format(t.date);
//            groupedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(t);
//        }
//
//        List<Object> groupedItems = new ArrayList<>();
//
//        int headerCounter = 0; // ✅ count only headers
//
//        for (Map.Entry<String, List<Transaction>> entry : groupedMap.entrySet()) {
//
//            List<Transaction> dailyList = entry.getValue();
//            double incomeTotal = 0, expenseTotal = 0;
//
//            // ✅ calculate totals
//            for (Transaction t : dailyList) {
//                if ("Income".equalsIgnoreCase(t.type)) {
//                    incomeTotal += t.amount;
//                } else if ("Expense".equalsIgnoreCase(t.type)) {
//                    expenseTotal += t.amount;
//                } else if ("TransferCredit".equalsIgnoreCase(t.type)) {
//                    incomeTotal += t.amount;
//                } else if ("TransferDebit".equalsIgnoreCase(t.type)) {
//                    expenseTotal += t.amount;
//                }
//            }
//
//            // ✅ BEFORE ADDING THIS HEADER → insert ad if required
//            if (adFrequency > 0 && headerCounter > 0 && headerCounter % adFrequency == 0) {
//                groupedItems.add("AD_PLACEHOLDER");
//            }
//
//            // ✅ insert header AFTER ad
//            DateHeader header;
//            try {
//                header = new DateHeader(dateOnlyFormat.parse(entry.getKey()), incomeTotal, expenseTotal);
//            } catch (Exception e) {
//                header = new DateHeader(new Date(), incomeTotal, expenseTotal);
//            }
//
//            groupedItems.add(header);
//            headerCounter++;
//
//            // ✅ add transactions normally (NO ADS HERE)
//            groupedItems.addAll(dailyList);
//        }
//
//        return groupedItems;
//    }



    // -------------------------------------------------------
    // ✅ Utility (Segment button style)
    // -------------------------------------------------------
    private void setFilterSelected(TextView selected, TextView... others) {
        int activeColor = getResources().getColor(R.color.nav_icon_active, getTheme());
        int defaultColor = getResources().getColor(R.color.nav_icon_default, getTheme());
        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(activeColor);
        for (TextView tv : others) {
            tv.setBackgroundResource(R.drawable.bg_segment_unselected);
            tv.setTextColor(defaultColor);
        }
    }
}
