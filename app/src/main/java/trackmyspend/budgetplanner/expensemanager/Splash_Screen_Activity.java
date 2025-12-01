package trackmyspend.budgetplanner.expensemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.clarity.Clarity;
import com.microsoft.clarity.ClarityConfig;
import com.microsoft.clarity.models.LogLevel;

import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.DatabaseDebugger;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.Onboarding_Activity;
import trackmyspend.budgetplanner.expensemanager.Util.ThemeManager;
import trackmyspend.budgetplanner.expensemanager.Util.UpdateActivity;

public class Splash_Screen_Activity extends AppCompatActivity {

    private AppDatabase db;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean adsLoaded = false;
    private boolean delayPassed = false;


    private String firebaseVersion;
    private boolean isRequiredToUpdate = false;
    private boolean firebaseLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 1️⃣ Load saved theme from SharedPreferences
        String mode = ThemeManager.getTheme(this);
        ThemeManager.applyTheme(mode);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ClarityConfig config = new ClarityConfig("uepwmjzs8a");
        config.setLogLevel(LogLevel.None);  // default is None
        Clarity.initialize(getApplicationContext(), config);

        // Now db is initialized → safe
        db = AppDatabase.getDatabase(this);
        DatabaseDebugger.logDatabase(db);

        // ----------------------------------------------------------
        // 4️⃣ Continue your splash flow
        // ----------------------------------------------------------
        if (!isInternetAvailable()) {
            showNoInternetDialog();
            return;
        }

        FirebaseApp.initializeApp(this);

        AdsManager.initialize(this, this::onAdsInitialized);
        fetchVersionFromFirebase();

        handler.postDelayed(() -> {
            delayPassed = true;
            maybeProceed();
//            Toast.makeText(this, "dealy", Toast.LENGTH_SHORT).show();
            Log.d("Splash_Screen_Activity", "delayPassed main : "+delayPassed);
        }, 3000);
    }


    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    if (isInternetAvailable()) {
                        recreate();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    private void onAdsInitialized() {
        adsLoaded = true;
        maybeProceed();
        Log.d("Splash_Screen_Activity", "adsLoaded: "+adsLoaded);
//        Toast.makeText(Splash_Screen_Activity.this, "ads loaded", Toast.LENGTH_SHORT).show();
    }

    private void fetchVersionFromFirebase() {
        FirebaseDatabase.getInstance("https://trackmyspend-expense-manager-default-rtdb.firebaseio.com/")
                .getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            firebaseVersion = snapshot.child("app-version").getValue(String.class);
                            Boolean required = snapshot.child("isRequiredToUpdate").getValue(Boolean.class);
                            isRequiredToUpdate = required != null && required;
                        }
                        firebaseLoaded = true;
//                        Toast.makeText(Splash_Screen_Activity.this, "firebase loaded", Toast.LENGTH_SHORT).show();
                        Log.d("Splash_Screen_Activity", "firebase loaded: "+firebaseLoaded);
                        maybeProceed();

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        firebaseLoaded = true;
                        maybeProceed();
//                        Toast.makeText(Splash_Screen_Activity.this, "firebase error", Toast.LENGTH_SHORT).show();
                        Log.d("Splash_Screen_Activity", "firebase error: "+firebaseLoaded);

                    }
                });
    }

    private void maybeProceed() {
        if (adsLoaded && delayPassed && firebaseLoaded) {
            proceedToNextScreen();
            Log.d("Splash_Screen_Activity", "delayPassed: "+delayPassed);
            Log.d("Splash_Screen_Activity", "adsLoaded: "+adsLoaded);
            Log.d("Splash_Screen_Activity", "firebaseLoaded: "+firebaseLoaded);
        }
    }


    private void proceedToNextScreen() {
//    Toast.makeText(this, "proceed", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // ✅ Fetch the actual app version dynamically (no hardcoding)
                String currentVersion = getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionName;

                if (isRequiredToUpdate && firebaseVersion != null && !firebaseVersion.equals(currentVersion)) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, UpdateActivity.class);
                        intent.putExtra("latestVersion", firebaseVersion);
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                User user = db.userDao().getFirstUser();
                Intent intent;

                if (user == null || TextUtils.isEmpty(user.name)) {
                    intent = new Intent(this, Onboarding_Activity.class);
                    intent.putExtra("step", "name");
                } else if (TextUtils.isEmpty(user.currency_code)) {
                    intent = new Intent(this, Onboarding_Activity.class);
                    intent.putExtra("step", "currency");
                } else {
                    intent = new Intent(this, MainActivity.class);
                }

                runOnUiThread(() -> {
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
