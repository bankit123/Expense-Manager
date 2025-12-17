//package trackmyspend.budgetplanner.expensemanager;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatDelegate;
//
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.microsoft.clarity.Clarity;
//import com.microsoft.clarity.ClarityConfig;
//import com.microsoft.clarity.models.LogLevel;
//
//import java.util.concurrent.Executors;
//
//import trackmyspend.budgetplanner.expensemanager.Ads.AdsManager;
//import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
//import trackmyspend.budgetplanner.expensemanager.DB.DatabaseDebugger;
//import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
//import trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.Onboarding_Activity;
//import trackmyspend.budgetplanner.expensemanager.Util.ThemeManager;
//import trackmyspend.budgetplanner.expensemanager.Util.UpdateActivity;
//
//public class Splash_Screen_Activity extends AppCompatActivity {
//
//    private AppDatabase db;
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private boolean adsLoaded = false;
//    private boolean delayPassed = false;
//
//
//    private String firebaseVersion;
//    private boolean isRequiredToUpdate = false;
//    private boolean firebaseLoaded = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        // 1️⃣ Load saved theme from SharedPreferences
//        String mode = ThemeManager.getTheme(this);
//        ThemeManager.applyTheme(mode);
//
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_splash_screen);
//
//        ClarityConfig config = new ClarityConfig("uepwmjzs8a");
//        config.setLogLevel(LogLevel.None);  // default is None
//        Clarity.initialize(getApplicationContext(), config);
//
//        // Now db is initialized → safe
//        db = AppDatabase.getDatabase(this);
//        DatabaseDebugger.logDatabase(db);
//
//        // ----------------------------------------------------------
//        // 4️⃣ Continue your splash flow
//        // ----------------------------------------------------------
//        if (!isInternetAvailable()) {
//            showNoInternetDialog();
//            return;
//        }
//
//        FirebaseApp.initializeApp(this);
//
//        AdsManager.initialize(this, this::onAdsInitialized);
//        fetchVersionFromFirebase();
//
//        handler.postDelayed(() -> {
//            delayPassed = true;
//            maybeProceed();
////            Toast.makeText(this, "dealy", Toast.LENGTH_SHORT).show();
//            Log.d("Splash_Screen_Activity", "delayPassed main : "+delayPassed);
//        }, 3000);
//    }
//
//
//    private boolean isInternetAvailable() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm != null) {
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            return activeNetwork != null && activeNetwork.isConnected();
//        }
//        return false;
//    }
//
//    private void showNoInternetDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("No Internet Connection")
//                .setMessage("Please check your internet connection and try again.")
//                .setCancelable(false)
//                .setPositiveButton("Retry", (dialog, which) -> {
//                    dialog.dismiss();
//                    if (isInternetAvailable()) {
//                        recreate();
//                    } else {
//                        showNoInternetDialog();
//                    }
//                })
//                .setNegativeButton("Exit", (dialog, which) -> {
//                    dialog.dismiss();
//                    finish();
//                })
//                .show();
//    }
//
//    private void onAdsInitialized() {
//        adsLoaded = true;
//        maybeProceed();
//        Log.d("Splash_Screen_Activity", "adsLoaded: "+adsLoaded);
////        Toast.makeText(Splash_Screen_Activity.this, "ads loaded", Toast.LENGTH_SHORT).show();
//    }
//
//    private void fetchVersionFromFirebase() {
//        FirebaseDatabase.getInstance("https://trackmyspend-expense-manager-default-rtdb.firebaseio.com/")
//                .getReference()
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            firebaseVersion = snapshot.child("app-version").getValue(String.class);
//                            Boolean required = snapshot.child("isRequiredToUpdate").getValue(Boolean.class);
//                            isRequiredToUpdate = required != null && required;
//                        }
//                        firebaseLoaded = true;
////                        Toast.makeText(Splash_Screen_Activity.this, "firebase loaded", Toast.LENGTH_SHORT).show();
//                        Log.d("Splash_Screen_Activity", "firebase loaded: "+firebaseLoaded);
//                        maybeProceed();
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        firebaseLoaded = true;
//                        maybeProceed();
////                        Toast.makeText(Splash_Screen_Activity.this, "firebase error", Toast.LENGTH_SHORT).show();
//                        Log.d("Splash_Screen_Activity", "firebase error: "+firebaseLoaded);
//
//                    }
//                });
//    }
//
//    private void maybeProceed() {
//        if (adsLoaded && delayPassed && firebaseLoaded) {
//            proceedToNextScreen();
//            Log.d("Splash_Screen_Activity", "delayPassed: "+delayPassed);
//            Log.d("Splash_Screen_Activity", "adsLoaded: "+adsLoaded);
//            Log.d("Splash_Screen_Activity", "firebaseLoaded: "+firebaseLoaded);
//        }
//    }
//
//
//    private void proceedToNextScreen() {
////    Toast.makeText(this, "proceed", Toast.LENGTH_SHORT).show();
//
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                // ✅ Fetch the actual app version dynamically (no hardcoding)
//                String currentVersion = getPackageManager()
//                        .getPackageInfo(getPackageName(), 0)
//                        .versionName;
//
//                if (isRequiredToUpdate && firebaseVersion != null && !firebaseVersion.equals(currentVersion)) {
//                    runOnUiThread(() -> {
//                        Intent intent = new Intent(this, UpdateActivity.class);
//                        intent.putExtra("latestVersion", firebaseVersion);
//                        startActivity(intent);
//                        finish();
//                    });
//                    return;
//                }
//
//                User user = db.userDao().getFirstUser();
//                Intent intent;
//
//                if (user == null || TextUtils.isEmpty(user.name)) {
//                    intent = new Intent(this, Onboarding_Activity.class);
//                    intent.putExtra("step", "name");
//                } else if (TextUtils.isEmpty(user.currency_code)) {
//                    intent = new Intent(this, Onboarding_Activity.class);
//                    intent.putExtra("step", "currency");
//                } else {
//                    intent = new Intent(this, MainActivity.class);
//                }
//
//                runOnUiThread(() -> {
//                    startActivity(intent);
//                    finish();
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//}

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.AdSettings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.clarity.Clarity;
import com.microsoft.clarity.ClarityConfig;
import com.microsoft.clarity.models.LogLevel;

import java.util.concurrent.Executors;


import trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.DatabaseDebugger;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.Onboarding_Activity;
import trackmyspend.budgetplanner.expensemanager.Util.ThemeManager;
import trackmyspend.budgetplanner.expensemanager.Util.UpdateActivity;

public class Splash_Screen_Activity extends AppCompatActivity {

    private static final String TAG = "SplashScreen";

    private AppDatabase db;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean delayPassed = false;
    private boolean firebaseLoaded = false;
    private boolean adsFinished = false; // ✅ success OR fail

    private String firebaseVersion;
    private boolean isRequiredToUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌗 Apply theme
        ThemeManager.applyTheme(ThemeManager.getTheme(this));
        com.facebook.ads.AudienceNetworkAds.initialize(this);
        AdSettings.addTestDevice("1cfdec71-440b-467a-956b-40fe5ea023ec");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        // 🔍 Clarity
        ClarityConfig config = new ClarityConfig("uepwmjzs8a");
        config.setLogLevel(LogLevel.None);
        Clarity.initialize(getApplicationContext(), config);

        db = AppDatabase.getDatabase(this);
        DatabaseDebugger.logDatabase(db);

        if (!isInternetAvailable()) {
            showNoInternetDialog();
            return;
        }

        FirebaseApp.initializeApp(this);

        // ⏱️ Minimum splash delay
        handler.postDelayed(() -> {
            delayPassed = true;
            maybeProceed();
            Log.d(TAG, "delayPassed");
        }, 3000);

        // 🔥 Ads init (NEW CALLBACK)
        AdsManager.initialize(this, new AdsManager.InitCallback() {
            @Override
            public void onReady() {
                adsFinished = true;
                Log.d(TAG, "Ads ready");
                maybeProceed();
            }

            @Override
            public void onFailed() {
                adsFinished = true; // ❗ Do not block splash
                Log.w(TAG, "Ads failed → continue");
                maybeProceed();
            }
        });

        // 🔄 Firebase version check
        fetchVersionFromFirebase();
    }

    // --------------------------------------------------
    // Firebase Version
    // --------------------------------------------------

    private void fetchVersionFromFirebase() {
        FirebaseDatabase.getInstance()
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
                        Log.d(TAG, "Firebase loaded");
                        maybeProceed();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        firebaseLoaded = true;
                        Log.e(TAG, "Firebase error");
                        maybeProceed();
                    }
                });
    }

    // --------------------------------------------------
    // Gatekeeper
    // --------------------------------------------------

    private void maybeProceed() {
        if (delayPassed && firebaseLoaded && adsFinished) {
            proceedToNextScreen();
        }
    }

    // --------------------------------------------------
    // Navigation
    // --------------------------------------------------

    private void proceedToNextScreen() {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String currentVersion = getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionName;

                if (isRequiredToUpdate
                        && firebaseVersion != null
                        && !firebaseVersion.equals(currentVersion)) {

                    runOnUiThread(() -> {
                        startActivity(new Intent(this, UpdateActivity.class));
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
                    intent = new Intent(this, AdsTestActivity.class);
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

    // --------------------------------------------------
    // Network
    // --------------------------------------------------

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo network = cm.getActiveNetworkInfo();
            return network != null && network.isConnected();
        }
        return false;
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet")
                .setMessage("Please check your internet connection.")
                .setCancelable(false)
                .setPositiveButton("Retry", (d, w) -> recreate())
                .setNegativeButton("Exit", (d, w) -> finish())
                .show();
    }
}

