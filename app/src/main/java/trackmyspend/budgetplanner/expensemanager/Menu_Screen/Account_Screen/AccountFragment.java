package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter.AccountAdapter;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;
import trackmyspend.budgetplanner.expensemanager.Util.ReviewUtils;

import java.util.List;


public class AccountFragment extends Fragment {

    // TextView tvWeekly, tvMonthly, tvYearly, tvPeriod;
    TextView tvAvailableBalance, tvAvailableCredit;
    RecyclerView rvAccounts;
    ImageView btnAddAccount;

    private AppDatabase db;
    private long userId = 1; // Example: replace with actual logged-in userId

    public AccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        ReviewUtils.showInAppReview(requireActivity());

        btnAddAccount = view.findViewById(R.id.btnAddAccount);
        btnAddAccount.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Add_Account_Activity.class);
            startActivity(intent);
        });

        FrameLayout bannerContainer = view.findViewById(R.id.banner_container);
        PriorityBannerController.show(
                requireActivity(),
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );

        tvAvailableBalance = view.findViewById(R.id.tvAvailableBalance);
//        tvAvailableCredit = view.findViewById(R.id.tvAvailableCredit);

        rvAccounts = view.findViewById(R.id.rvAccounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getDatabase(requireContext());

        // Observe accounts
        db.accountDao().getAccountsByUser(userId).observe(getViewLifecycleOwner(), accounts -> {
            AccountAdapter adapter = new AccountAdapter(requireContext(), accounts);
            rvAccounts.setAdapter(adapter);

            // ✅ Update balances
            updateBalances(accounts);
        });

        return view;
    }

    /**
     * ✅ Function to update cash & credit balances
     */
    private void updateBalances(List<Account> accounts) {
        double Balance = 0.0;


        for (Account account : accounts) {

            Balance += account.amount;

        }

        // ✅ Use CurrencyFormatterUtil to dynamically format with user's currency & locale
        tvAvailableBalance.setText(CurrencyFormatterUtil.format(Balance));
    }

}