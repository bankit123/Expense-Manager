package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ShakeUtil {

    /**
     * Shake any view + vibrate for strong visual feedback.
     */
    public static void shake(Context context, View v) {

        // --- Shake Animation (bigger + quicker movement) ---
        TranslateAnimation shake = new TranslateAnimation(
                0, 25,   // fromXDelta, toXDelta (increased movement)
                0, 0
        );
        shake.setDuration(60);            // fast shake
        shake.setRepeatMode(Animation.REVERSE);
        shake.setRepeatCount(4);          // more shakes
        v.startAnimation(shake);

        // --- Vibration ---
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    60,                      // duration
                                    VibrationEffect.DEFAULT_AMPLITUDE
                            )
                    );
                } else {
                    vibrator.vibrate(60);
                }
            }
        } catch (Exception ignored) {}
    }
}
