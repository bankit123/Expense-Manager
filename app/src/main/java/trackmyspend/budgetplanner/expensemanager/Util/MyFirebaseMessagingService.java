package trackmyspend.budgetplanner.expensemanager.Util;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Splash_Screen_Activity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
/**
 * 🔥 MyFirebaseMessagingService
 * Handles receiving FCM messages and showing push notifications (with optional image support).
 *
 * Supports both:
 *  - Notification payload (sent from Firebase Console)
 *  - Data payload (custom key-value via API)
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "default_channel";

    /**
     * Called when a new FCM registration token is generated.
     * Useful for sending the token to your server.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🎯 New FCM Token: " + token);
        // TODO: send token to your backend if required
    }

    /**
     * Called when a new push message is received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "📩 Message received from: " + remoteMessage.getFrom());

        String title = null;
        String body = null;
        String imageUrl = null;

        // ✅ Handle Notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Payload: " + title + " - " + body);
        }

        // ✅ Handle Data payload (for custom notifications or rich media)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("title"))
                title = remoteMessage.getData().get("title");
            if (remoteMessage.getData().containsKey("body"))
                body = remoteMessage.getData().get("body");
            if (remoteMessage.getData().containsKey("image"))
                imageUrl = remoteMessage.getData().get("image");
        }

        if (title != null || body != null) {
            showNotification(title, body, imageUrl);
        }
    }

    /**
     * 🔔 Displays a rich notification with optional image support.
     */
    private void showNotification(String title, String message, String imageUrl) {
        createNotificationChannel();

        // Intent when user taps the notification
        Intent intent = new Intent(this, Splash_Screen_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // ✅ change icon per your brand
                .setContentTitle(title != null ? title : getString(R.string.app_name))
                .setContentText(message != null ? message : "")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent);

        // ✅ Optional: load and show image in BigPictureStyle
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            try {
//                Bitmap bitmap = Glide.with(this)
//                        .asBitmap()
//                        .load(imageUrl)
//                        .submit()
//                        .get();
//
//                builder.setStyle(new NotificationCompat.BigPictureStyle()
//                        .bigPicture(bitmap)
//                        .bigLargeIcon(null));
//
//            } catch (Exception e) {
//                Log.e(TAG, "⚠️ Error loading image: " + e.getMessage());
//            }
//        } else {
//            // ✅ Default style with expanded text
//            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
//        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        // Android 13+ requires POST_NOTIFICATIONS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "⚠️ Notification permission not granted");
            return;
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * 📢 Creates notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "App Notifications";
            String description = "Default channel for app notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
