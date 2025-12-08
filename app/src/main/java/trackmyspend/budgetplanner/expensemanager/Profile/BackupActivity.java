package trackmyspend.budgetplanner.expensemanager.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.raphaelebner.roomdatabasebackup.core.RoomBackup;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Splash_Screen_Activity;

public class BackupActivity extends AppCompatActivity {

    private RoomBackup roomBackup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backup);

        LinearLayout btnBackup = findViewById(R.id.layoutBackup);
        LinearLayout btnRestore = findViewById(R.id.layoutRestore);

        roomBackup = new RoomBackup(this);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());


        btnBackup.setOnClickListener(v -> startBackup());
        btnRestore.setOnClickListener(v -> startRestore());
    }

    private void startBackup() {
        roomBackup
                .database(AppDatabase.getDatabase(this)) // active DB
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .backupIsEncrypted(false)
                .onCompleteListener((success, message, exitCode) -> {
                    if (success) {
                        Toast.makeText(this, "✅ Backup successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Backup failed: " + message, Toast.LENGTH_LONG).show();
                    }
                })
                .backup();
    }

    private void startRestore() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Restore Backup")
                .setMessage("Restoring will overwrite your current data. Do you want to continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // ✅ Close old DB
                    AppDatabase.closeDatabase();

                    // ✅ Rebuild a fresh DB instance for restore
                    AppDatabase newDb = AppDatabase.getDatabase(this);

                    roomBackup
                            .database(newDb)
                            .enableLogDebug(true)
                            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                            .backupIsEncrypted(false)
                            .onCompleteListener((success, message, exitCode) -> {
                                if (success) {
                                    new MaterialAlertDialogBuilder(this)
                                            .setTitle("Restore Successful")
                                            .setMessage("Your data has been restored. The app will now restart.")
                                            .setCancelable(false)
                                            .setPositiveButton("Restart Now", (d, w) -> restartApp())
                                            .show();
                                } else {
                                    Toast.makeText(this, "❌ Restore failed: " + message, Toast.LENGTH_LONG).show();
                                }
                            })
                            .restore();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void restartApp() {
        Intent i = new Intent(this, Splash_Screen_Activity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        // Kill process so Room reloads new DB
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}