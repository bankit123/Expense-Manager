package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.AdManage.FacebookNativeAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.GoogleNativeAdHelper;
import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;

import trackmyspend.budgetplanner.expensemanager.AdsTestActivity;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.MainActivity;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Recurring_Payment_Activity;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.DateHeader;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter.TransactionGroupedAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.FilterUtil;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.MemoryVariable;
import trackmyspend.budgetplanner.expensemanager.Profile.Profile_Activity;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView rvTransactions;
    LinearLayout layoutEmptyState;
    private LinearLayout payRecurring, addRecurring;
    private AppDatabase db;
    private TransactionGroupedAdapter adapter;

    private String currencySymbol = "₹";
    private String localeTag = "en_IN";

    private TextView tvWeekly, tvMonthly, tvYearly, tvPeriod, tvPersonName, tvCurrencyText;
    private TextView tvTotalIncome, tvTotalExpense, tvBalance; // ✅ totals
    private TextView tvTransactionTitle, tvTransactionSubtitle;
    private final SimpleDateFormat rangeFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private boolean nativeRefreshDone = false;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        tvPersonName = view.findViewById(R.id.tvPersonName);
        tvCurrencyText = view.findViewById(R.id.tvCurrencyText);

        tvWeekly = view.findViewById(R.id.tvWeekly);
        tvMonthly = view.findViewById(R.id.tvMonthly);
        tvYearly = view.findViewById(R.id.tvYearly);
        tvPeriod = view.findViewById(R.id.tvPeriod);

        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvBalance = view.findViewById(R.id.tvBalance);

        tvTransactionTitle = view.findViewById(R.id.tvTransactionTitle);
        tvTransactionSubtitle = view.findViewById(R.id.tvTransactionSubtitle);

        tvTransactionTitle.setText("Transaction History");

        FrameLayout bannerContainer = view.findViewById(R.id.banner_container);
        PriorityBannerController.show(
                requireActivity(),
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")

        );



        addRecurring = view.findViewById(R.id.addRecurring);
        payRecurring = view.findViewById(R.id.payRecurring);

        addRecurring.setOnClickListener(v ->{
            Intent intent = new Intent(getContext(), Recurring_Payment_Activity.class);
            startActivity(intent);
        });

        payRecurring.setOnClickListener(v ->{
            Intent intent = new Intent(getContext(), Pay_Recurring_Activity.class);
            startActivity(intent);
        });


        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Profile_Activity.class);
            startActivity(intent);
        });

        db = AppDatabase.getDatabase(requireContext());

        adapter = new TransactionGroupedAdapter(requireContext(), new ArrayList<>(), db);
        rvTransactions.setAdapter(adapter);

        long userId = 1; // 🔥 Replace with logged-in user id

        loadUserInfo();
        // Default → Weekly
        setFilterSelected(tvWeekly, tvMonthly, tvYearly, tvPeriod);
        FilterUtil.getWeeklyRange((start, end, label) -> {
            tvTransactionSubtitle.setText(label);
            fetchTransactionsForPeriod(userId, start, end);
        });


        // inside onCreateView or similar
        tvWeekly.setOnClickListener(v -> {
            setFilterSelected(tvWeekly, tvMonthly, tvYearly, tvPeriod);
            FilterUtil.getWeeklyRange((start, end, label) -> {
                tvTransactionSubtitle.setText(label);
                fetchTransactionsForPeriod(userId, start, end);
            });
        });

        tvMonthly.setOnClickListener(v -> {
            setFilterSelected(tvMonthly, tvWeekly, tvYearly, tvPeriod);
            FilterUtil.getMonthlyRange((start, end, label) -> {
                tvTransactionSubtitle.setText(label);
                fetchTransactionsForPeriod(userId, start, end);
            });
        });

        tvYearly.setOnClickListener(v -> {
            setFilterSelected(tvYearly, tvWeekly, tvMonthly, tvPeriod);
            FilterUtil.getYearlyRange((start, end, label) -> {
                tvTransactionSubtitle.setText(label);
                fetchTransactionsForPeriod(userId, start, end);
            });
        });

        tvPeriod.setOnClickListener(v -> {
            setFilterSelected(tvPeriod, tvWeekly, tvMonthly, tvYearly);
            FilterUtil.showPeriodPicker(requireContext(), getParentFragmentManager(), (start, end, label) -> {
                tvTransactionSubtitle.setText(label);
                fetchTransactionsForPeriod(userId, start, end);
            });
        });

        return view;
    }

    private synchronized void triggerNativeRefreshOnce() {
        if (nativeRefreshDone) return;

        nativeRefreshDone = true;

        if (adapter != null && isAdded()) {
            requireActivity().runOnUiThread(() ->
                    adapter.notifyDataSetChanged()
            );
        }
    }


    private void loadUserInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().getFirstUser();
            if (user != null) {
                String name = user.name != null ? user.name.trim() : "";

                // ✅ Capitalize first letter
                if (!name.isEmpty()) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }

                String displayName = !name.isEmpty()
                        ? "Hey, " + name + "!"
                        : "Hey there!";

                String currencyDisplay = user.currency_symbol + " - " +
                        user.currency_name + " (" + user.currency_code + ")";

                currencySymbol = user.currency_symbol; // store symbol globally

                requireActivity().runOnUiThread(() -> {
                    tvPersonName.setText(displayName);
                    tvCurrencyText.setText(currencyDisplay);
                });
            }
        });
    }


    // 🔹 Highlight selected filter
    private void setFilterSelected(TextView selected, TextView... others) {
        int activeColor = requireContext().getResources().getColor(R.color.nav_icon_active);
        int defaultColor = requireContext().getResources().getColor(R.color.nav_icon_default);

        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(activeColor);

        for (TextView tv : others) {
            tv.setBackgroundResource(R.drawable.bg_segment_unselected);
            tv.setTextColor(defaultColor);
        }
    }


    // 🔹 Fetch transactions between two dates
    @SuppressLint("SetTextI18n")
    private void fetchTransactionsForPeriod(long userId, Date start, Date end) {
        db.transactionDao().getTransactionsByPeriod(userId, start, end)
                .observe(getViewLifecycleOwner(), transactions -> {
                    List<Object> items = prepareGroupedItems(transactions);

                    if (items.isEmpty()) {
                        rvTransactions.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvTransactions.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    adapter.updateData(items);

                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (Transaction t : transactions) {
                        if ("Income".equalsIgnoreCase(t.type)) {
                            totalIncome += t.amount;
                        } else if ("Expense".equalsIgnoreCase(t.type)){
                            totalExpense += t.amount;
                        }
                    }

                    double balance = totalIncome - totalExpense;

                    // ✅ Dynamic locale formatting using cached CurrencyFormatterUtil values
                    tvTotalIncome.setText(CurrencyFormatterUtil.format(totalIncome));
                    tvTotalExpense.setText(CurrencyFormatterUtil.format(totalExpense));
                    tvBalance.setText("Balance: " + CurrencyFormatterUtil.format(balance));
                });
    }

    private List<Object> prepareGroupedItems(List<Transaction> transactions) {

        List<Object> items = new ArrayList<>();
        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();
        SimpleDateFormat keyFormat =
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        // 🔥 NEW Firebase-based ad frequency (as requested)
        int adFrequency = 0;

        if (trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig() != null) {
            String cnt =
                    trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager
                            .getConfig()
                            .get("b_home_trans_ads_cnt"); // Firebase key

            try {
                adFrequency = Integer.parseInt(cnt);
            } catch (Exception ignored) {
                adFrequency = 0;
            }
        }

        int txnCounter = 0; // counts ONLY transactions (not headers)

        // ---------------- GROUP BY DAY ----------------
        for (Transaction txn : transactions) {
            String key = keyFormat.format(txn.date);
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<>());
            }
            grouped.get(key).add(txn);
        }

        // ---------------- BUILD ITEMS ----------------
        for (List<Transaction> dayTxns : grouped.values()) {

            DateHeader header =
                    new DateHeader(dayTxns.get(0).date, 0, 0);

            for (Transaction t : dayTxns) {
                if ("Income".equalsIgnoreCase(t.type)) {
                    header.incomeTotal += t.amount;
                } else if ("Expense".equalsIgnoreCase(t.type)) {
                    header.expenseTotal += t.amount;
                }
            }

            items.add(header);

            for (Transaction t : dayTxns) {
                items.add(t);
                txnCounter++;

                // 🔥 Insert ad after every N transactions
                if (adFrequency > 0 && txnCounter % adFrequency == 0) {
                    items.add("AD_PLACEHOLDER");
                }
            }
        }

        return items;
    }



    @Override
    public void onResume() {
        super.onResume();
        //checkAndShowSupportDialog();
    }



}
