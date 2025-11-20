package trackmyspend.budgetplanner.expensemanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import com.facebook.FacebookSdk;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.DatabaseDebugger;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.AccountFragment;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.AddTransactionActivity;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Add_Transaction.Recurring_Payment.Worker.RecurringSchedulerWorker;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Analysis_Screen.AnalysisFragment;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Backup_Screen.Backup_Fragment;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.HomeFragment;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen.ReminderFragment;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    LinearLayout navHome, navAnalysis, navAccount, navReminder, navBackup;
    ImageView iconHome, iconAnalysis, iconAccount, iconReminder, iconBackup;
    TextView titleHome, titleAnalysis, titleAccount, titleReminder, titleBackup;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private long backPressedTime = 0;

    // Fragments
    Fragment homeFragment, analysisFragment, accountFragment, reminderFragment, backupFragment;
    Fragment activeFragment;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        AppDatabase db = AppDatabase.getDatabase(this);
        DatabaseDebugger.logDatabase(db);
        CurrencyFormatterUtil.init(getApplicationContext());


//        setupRecurringWorker();
        WorkManager.getInstance(this).enqueue(
                new OneTimeWorkRequest.Builder(RecurringSchedulerWorker.class).build()
        );

        requestNotificationPermission();

        // Initialize nav items
        navHome = findViewById(R.id.nav_home);
        navAnalysis = findViewById(R.id.nav_analysis);
        navAccount = findViewById(R.id.nav_account);
        navReminder = findViewById(R.id.nav_reminder);
        navBackup = findViewById(R.id.nav_backup); // ✅ NEW

        iconHome = findViewById(R.id.icon_home);
        iconAnalysis = findViewById(R.id.icon_analysis);
        iconAccount = findViewById(R.id.icon_account);
        iconReminder = findViewById(R.id.icon_reminder);
        iconBackup = findViewById(R.id.icon_backup); // ✅ NEW

        titleHome = findViewById(R.id.title_home);
        titleAnalysis = findViewById(R.id.title_analysis);
        titleAccount = findViewById(R.id.title_account);
        titleReminder = findViewById(R.id.title_reminder);
        titleBackup = findViewById(R.id.title_backup); // ✅ NEW

        findViewById(R.id.fab_add_transaction).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTransactionActivity.class));
        });

        // Initialize fragments
        homeFragment = new HomeFragment();
        analysisFragment = new AnalysisFragment();
        accountFragment = new AccountFragment();
        reminderFragment = new ReminderFragment();
        backupFragment = new Backup_Fragment(); // ✅ NEW
        activeFragment = homeFragment;

        // Add all fragments once
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, homeFragment, "HOME");
        transaction.add(R.id.container, analysisFragment, "ANALYSIS").hide(analysisFragment);
        transaction.add(R.id.container, accountFragment, "ACCOUNT").hide(accountFragment);
        transaction.add(R.id.container, reminderFragment, "REMINDER").hide(reminderFragment);
        transaction.add(R.id.container, backupFragment, "BACKUP").hide(backupFragment); // ✅ NEW
        transaction.commit();

        // Set default active
        setActive(navHome, iconHome, titleHome);

        // Click listeners
        navHome.setOnClickListener(v -> showFragment(homeFragment, navHome, iconHome, titleHome));
        navAnalysis.setOnClickListener(v -> showFragment(analysisFragment, navAnalysis, iconAnalysis, titleAnalysis));
        navAccount.setOnClickListener(v -> showFragment(accountFragment, navAccount, iconAccount, titleAccount));
        navReminder.setOnClickListener(v -> showFragment(reminderFragment, navReminder, iconReminder, titleReminder));
        navBackup.setOnClickListener(v -> showFragment(backupFragment, navBackup, iconBackup, titleBackup)); // ✅ NEW

        // ✅ Upload user info to Firestore once app is fully opened
        uploadUserToFirestore(db);
    }

    private void showFragment(Fragment fragment, LinearLayout nav, ImageView icon, TextView title) {
        if (fragment != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(activeFragment).show(fragment).commit();
            activeFragment = fragment;
            setActive(nav, icon, title);
        }
    }

    private void setActive(LinearLayout activeNav, ImageView activeIcon, TextView activeTitle) {
        resetNavColors();
        int activeColor = getResources().getColor(R.color.nav_icon_active, getTheme());
        activeIcon.setColorFilter(activeColor);
        activeTitle.setTextColor(activeColor);
        animateBounce(activeNav);
    }

    private void resetNavColors() {
        int defaultColor = getResources().getColor(R.color.nav_icon_default, getTheme());
        iconHome.setColorFilter(defaultColor);
        iconAnalysis.setColorFilter(defaultColor);
        iconAccount.setColorFilter(defaultColor);
        iconReminder.setColorFilter(defaultColor);
        iconBackup.setColorFilter(defaultColor);
        titleHome.setTextColor(defaultColor);
        titleAnalysis.setTextColor(defaultColor);
        titleAccount.setTextColor(defaultColor);
        titleReminder.setTextColor(defaultColor);
        titleBackup.setTextColor(defaultColor);
    }

    private void animateBounce(LinearLayout view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.9f, 1.1f, 0.9f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(150);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
        view.startAnimation(scaleAnimation);
    }

    /**
     * ✅ Upload user info to Firestore (first-time only)
     */
    private void uploadUserToFirestore(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                User user = db.userDao().getFirstUser();
                if (user == null) return;

                // ✅ 1️⃣ SharedPreferences to track first upload
                android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                boolean alreadyUploaded = prefs.getBoolean("firestore_uploaded", false);

                if (alreadyUploaded) {
                    Log.i(TAG, "ℹ️ Firestore upload skipped — already uploaded before.");
                    return; // 👈 Skip upload if already done
                }

                // ✅ 2️⃣ Get device + user info
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", userId);
                userData.put("name", user.name != null ? user.name : "Guest User");
                userData.put("currency_name", user.currency_name != null ? user.currency_name : "");
                userData.put("mode", user.mode != null ? user.mode : "Light");
                userData.put("android_version", Build.VERSION.RELEASE);
                userData.put("device_brand", Build.MANUFACTURER + " " + Build.MODEL);
                userData.put("country", Locale.getDefault().getCountry());
                userData.put("created_at", FieldValue.serverTimestamp());

                // ✅ 3️⃣ Upload to Firestore only once
                firestore.collection("app_users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.i(TAG, "✅ User data uploaded to Firestore successfully");

                            // ✅ 4️⃣ Mark as uploaded so it doesn’t repeat next time
                            prefs.edit().putBoolean("firestore_uploaded", true).apply();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to upload user data: " + e.getMessage()));

            } catch (Exception e) {
                Log.e(TAG, "⚠️ Firestore upload error: " + e.getMessage());
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        UpdateUtils.checkForFlexibleUpdate(this);
//        UpdateUtils.resumeUpdateIfPending(this);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        UpdateUtils.unregisterListener();
//    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupRecurringWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                RecurringSchedulerWorker.class,
                30, TimeUnit.SECONDS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecurringCatchupWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        Fragment currentFragment = activeFragment;

        // ✅ If the current fragment is not HOME → go to HOME
        if (currentFragment != homeFragment) {
            showFragment(homeFragment, navHome, iconHome, titleHome);
            return;
        }

        // ✅ Double press logic
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }


}
