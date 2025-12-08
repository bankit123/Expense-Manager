package trackmyspend.budgetplanner.expensemanager.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.Currency_Input_Fragment;
import trackmyspend.budgetplanner.expensemanager.Profile.Categories.Categories_For_Profile;
import trackmyspend.budgetplanner.expensemanager.Profile.Subtype.Subtype_For_Profile_Activity;
import trackmyspend.budgetplanner.expensemanager.R;

public class Profile_Activity extends AppCompatActivity implements Currency_Input_Fragment.OnCurrencySavedListener {

    LinearLayout changeCurrency;
    ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // 🔹 Find the views
        changeCurrency = findViewById(R.id.changeCurrency);
        ivBack = findViewById(R.id.ivBack);

        // 🔹 Back button
        ivBack.setOnClickListener(v -> finish());

        // 🔹 Change currency
        changeCurrency.setOnClickListener(v -> openCurrencyChange());

        LinearLayout AccountLayout = findViewById(R.id.Account);
        AccountLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, Subtype_For_Profile_Activity.class);
            startActivity(intent);
        });

        // 🔹 Categories
        LinearLayout categoriesLayout = findViewById(R.id.categories);
        categoriesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, Categories_For_Profile.class);
            startActivity(intent);
        });

//        darkMode
        LinearLayout darkModeLayout = findViewById(R.id.darkMode);
        darkModeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, DarkMode_Activity.class);
            startActivity(intent);
        });

        // 🔹 About Us
        LinearLayout aboutUsLayout = findViewById(R.id.aboutUs);
        aboutUsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, AboutUs_Activity.class);
            startActivity(intent);
        });

//        backup
        LinearLayout backupLayout = findViewById(R.id.backup);
        backupLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, BackupActivity.class);
            startActivity(intent);
        });

        LinearLayout privacyPolicyLayout = findViewById(R.id.PrivacyPolicy);
        privacyPolicyLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Activity.this, Privacy_Policy_Activity.class);
            startActivity(intent);
        });

        LinearLayout share = findViewById(R.id.share);

        share.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TrackMySpend – Expense Manager");

            String shareMessage = "💰 TrackMySpend – Expense Manager 📊\n\n" +
                    "Take control of your money like a pro!\n" +
                    "Easily manage your daily expenses, track income, and stay on budget — all in one app.\n\n" +
                    "✨ Key Features:\n" +
                    "• 💵 Add & categorize your expenses\n" +
                    "• 📆 View weekly, monthly, or yearly stats\n" +
                    "• 📊 Smart charts & insights\n" +
                    "• 💡 Manage EMIs and bills\n" +
                    "• ☁️ Backup your data securely\n\n" +
                    "Download now and start tracking smarter!\n👇\n" +
                    "https://play.google.com/store/apps/details?id=" + getPackageName();

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

    }



    private void openCurrencyChange() {
        Currency_Input_Fragment fragment = new Currency_Input_Fragment();

        Bundle args = new Bundle();
        args.putBoolean("fromProfile", true);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ✅ Called when currency is updated
    @Override
    public void onCurrencySaved() {
        getSupportFragmentManager().popBackStack();
    }
}
