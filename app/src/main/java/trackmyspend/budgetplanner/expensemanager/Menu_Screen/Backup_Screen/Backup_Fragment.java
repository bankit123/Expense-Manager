package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Backup_Screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Splash_Screen_Activity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.raphaelebner.roomdatabasebackup.core.RoomBackup;

public class Backup_Fragment extends Fragment {

    private RoomBackup roomBackup;

    public Backup_Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup_, container, false);

        LinearLayout btnBackup = view.findViewById(R.id.layoutBackup);
        LinearLayout btnRestore = view.findViewById(R.id.layoutRestore);

        roomBackup = new RoomBackup(requireContext());

        btnBackup.setOnClickListener(v -> startBackup());
        btnRestore.setOnClickListener(v -> startRestore());

        return view;
    }

    private void startBackup() {
        roomBackup
                .database(AppDatabase.getDatabase(requireContext())) // active DB
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                .backupIsEncrypted(false)
                .onCompleteListener((success, message, exitCode) -> {
                    if (success) {
                        Toast.makeText(requireContext(), "✅ Backup successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "❌ Backup failed: " + message, Toast.LENGTH_LONG).show();
                    }
                })
                .backup();
    }

    private void startRestore() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restore Backup")
                .setMessage("Restoring will overwrite your current data. Do you want to continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // ✅ Close old DB
                    AppDatabase.closeDatabase();

                    // ✅ Rebuild a fresh DB instance for restore
                    AppDatabase newDb = AppDatabase.getDatabase(requireContext());

                    roomBackup
                            .database(newDb)
                            .enableLogDebug(true)
                            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                            .backupIsEncrypted(false)
                            .onCompleteListener((success, message, exitCode) -> {
                                if (success) {
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("Restore Successful")
                                            .setMessage("Your data has been restored. The app will now restart.")
                                            .setCancelable(false)
                                            .setPositiveButton("Restart Now", (d, w) -> restartApp())
                                            .show();
                                } else {
                                    Toast.makeText(requireContext(), "❌ Restore failed: " + message, Toast.LENGTH_LONG).show();
                                }
                            })
                            .restore();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void restartApp() {
        Intent i = new Intent(requireContext(), Splash_Screen_Activity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        // Kill process so Room reloads new DB
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
