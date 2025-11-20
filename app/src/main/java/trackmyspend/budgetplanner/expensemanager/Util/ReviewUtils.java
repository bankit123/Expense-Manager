package trackmyspend.budgetplanner.expensemanager.Util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

/**
 * Utility class for showing Google Play in-app review dialog.
 * Usage: ReviewUtils.showInAppReview(activity);
 */
public class ReviewUtils {

    private static final String TAG = "ReviewUtils";

    // 🔹 Call this function from any Activity to show in-app review
    public static void showInAppReview(Activity activity) {
        if (activity == null) return;

        ReviewManager manager = ReviewManagerFactory.create(activity);

        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // ✅ Successfully got ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);

                flow.addOnCompleteListener(flowTask -> {
                    // ✅ The review dialog was shown (user may or may not submit)
//                    Toast.makeText(activity, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                });

            } else {
                // ❌ Request failed — fallback to Play Store
                Log.e(TAG, "In-App Review request failed", task.getException());
//                openPlayStorePage(activity);
            }
        });
    }

    // 🔹 Fallback method to open app's Play Store page directly
    private static void openPlayStorePage(Activity activity) {
        String packageName = activity.getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }
}
