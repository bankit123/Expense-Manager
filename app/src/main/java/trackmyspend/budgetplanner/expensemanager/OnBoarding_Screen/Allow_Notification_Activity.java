package trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import trackmyspend.budgetplanner.expensemanager.MainActivity;
import trackmyspend.budgetplanner.expensemanager.R;

public class Allow_Notification_Activity extends AppCompatActivity {

    TextView btnAllow;

    // Android 13+ Permission Launcher
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                openMainScreen(); // proceed regardless of granted or denied
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allow_notification);

        btnAllow = findViewById(R.id.btnAllow);
//        btnSkip = findViewById(R.id.btnSkip);

        btnAllow.setOnClickListener(v -> requestNotificationPermission());
//        btnSkip.setOnClickListener(v -> openMainScreen());
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {

                openMainScreen(); // already allowed

            } else {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }

        } else {
            // below Android 13
            openMainScreen();
        }
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
