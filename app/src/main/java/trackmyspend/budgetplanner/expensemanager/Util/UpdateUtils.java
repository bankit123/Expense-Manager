package trackmyspend.budgetplanner.expensemanager.Util;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

/**
 * Utility class to handle Google Play In-App Update (Flexible Update).
 * Users can continue using the app while downloading the update.
 * Usage: UpdateUtils.checkForFlexibleUpdate(activity);
 */
public class UpdateUtils {

    private static final int UPDATE_REQUEST_CODE = 2001;
    private static final String TAG = "UpdateUtils";

    private static AppUpdateManager appUpdateManager;
    private static InstallStateUpdatedListener listener;

    public static void checkForFlexibleUpdate(Activity activity) {
        appUpdateManager = AppUpdateManagerFactory.create(activity);

        // ✅ Listen for download status changes
        listener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // ✅ When update download finishes, prompt user to restart
                showCompleteUpdateSnackbar(activity);
            }
        };

        appUpdateManager.registerListener(listener);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                try {
                    AppUpdateOptions options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build();
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            options,
                            UPDATE_REQUEST_CODE
                    );

                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Update flow failed", e);
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // ✅ If update is already downloaded, prompt user immediately
                showCompleteUpdateSnackbar(activity);
            } else {
                Log.i(TAG, "No update available");
            }
        }).addOnFailureListener(e ->
                Toast.makeText(activity, "Failed to check for updates", Toast.LENGTH_SHORT)
        );
    }

    // ✅ Show Snackbar to restart and install update
    private static void showCompleteUpdateSnackbar(Activity activity) {
        Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        "Update ready to install!",
                        Snackbar.LENGTH_INDEFINITE
                )
                .setAction("Restart", v -> {
                    appUpdateManager.completeUpdate();
                })
                .show();
    }

    // ✅ Should be called from Activity.onResume() to continue update if interrupted
    public static void resumeUpdateIfPending(Activity activity) {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(activity);
        }

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    AppUpdateOptions options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build();
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            options,
                            UPDATE_REQUEST_CODE
                    );
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Failed to resume update", e);
                }
            }
        });
    }

    // ✅ Unregister listener when not needed
    public static void unregisterListener() {
        if (appUpdateManager != null && listener != null) {
            appUpdateManager.unregisterListener(listener);
        }
    }
}
