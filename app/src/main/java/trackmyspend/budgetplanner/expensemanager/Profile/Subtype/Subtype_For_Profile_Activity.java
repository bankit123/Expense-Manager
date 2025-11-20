package trackmyspend.budgetplanner.expensemanager.Profile.Subtype;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Add_Account_Activity;
import trackmyspend.budgetplanner.expensemanager.Profile.Subtype.Adapter.AccountSubtypeGroupedAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

public class Subtype_For_Profile_Activity extends AppCompatActivity {

    private RecyclerView rvAccounts;
    private AppDatabase db;
    private AccountSubtypeGroupedAdapter adapter;
    private ImageView ivBack, btnAddAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subtype_for_profile);

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        btnAddAccount = findViewById(R.id.btnAddAccount);
        btnAddAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, Add_Account_Activity.class);
            startActivity(intent);
        });


        rvAccounts = findViewById(R.id.rvAccounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        db = AppDatabase.getDatabase(this);

        adapter = new AccountSubtypeGroupedAdapter(this, new ArrayList<>(), db);
        rvAccounts.setAdapter(adapter);

        observeAllData();
    }

    private void observeAllData() {
        // Observe both tables reactively
        db.accountDao().getAllAccountsLive().observe(this, accounts -> {
            if (accounts == null) return;

            db.subtypeDao().getAllSubtypesLive().observe(this, subtypes -> {
                if (subtypes == null) return;

                Executors.newSingleThreadExecutor().execute(() -> {
                    List<Object> combined = new ArrayList<>();

                    for (Account account : accounts) {
                        combined.add(account);

                        // Filter subtypes that belong to this account
                        for (Subtype subtype : subtypes) {
                            if (subtype.account_id == account.account_id) {
                                combined.add(subtype);
                            }
                        }
                    }

                    runOnUiThread(() -> adapter.updateData(combined));
                });
            });
        });
    }

}
