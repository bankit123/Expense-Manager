package trackmyspend.budgetplanner.expensemanager.Util;

import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Universal swipe-to-reveal helper used across adapters.
 * Smooth, bounce animation — supports RecyclerView interception fix and prevents stuck swipes.
 */
public class SwipeRevealHelper {

    private static View currentlySwiped = null; // allow only one open item at a time

    public static void attach(final View content, final View deleteBg, final Runnable onDeleteClick) {
        if (content == null || deleteBg == null) return;

        final float MAX_SWIPE = 0.25f; // reveal 25% width
        final float AUTO_OPEN_THRESHOLD = 0.05f; // swipe 5% width to open
        final float TAP_SLOP = 10f; // small move tolerance
        final float VERTICAL_SLOP = 25f;

        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final boolean[] isSwiping = {false};
        final boolean[] isOpen = {false};
        final boolean[] isVertical = {false};

        content.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    isSwiping[0] = false;
                    isVertical[0] = false;

                    // Close previously open item
                    if (currentlySwiped != null && currentlySwiped != content) {
                        View oldBg = ((View) currentlySwiped.getParent())
                                .findViewById(deleteBg.getId());
                        closeSwipe(currentlySwiped, oldBg);
                        currentlySwiped = null;
                    }

                    // Stop RecyclerView intercept
                    if (v.getParent() != null)
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float deltaX = moveX - downX[0];
                    float deltaY = moveY - downY[0];

                    // Detect vertical scroll
                    if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > VERTICAL_SLOP) {
                        isVertical[0] = true;
                        if (v.getParent() != null)
                            v.getParent().requestDisallowInterceptTouchEvent(false);

                        // ✅ Auto-close if vertical scroll starts while mid-swipe
                        if (Math.abs(content.getTranslationX()) > 10f) {
                            closeSwipe(content, deleteBg);
                            isOpen[0] = false;
                            currentlySwiped = null;
                        }
                        return false;
                    }

                    if (Math.abs(deltaX) > TAP_SLOP && !isVertical[0]) {
                        isSwiping[0] = true;
                    }

                    if (isSwiping[0] && deltaX < 0) { // left swipe
                        deleteBg.setVisibility(View.VISIBLE);

                        float maxTranslate = v.getWidth() * MAX_SWIPE;
                        float translation = Math.max(deltaX, -maxTranslate);
                        v.setTranslationX(translation);

                        float factor = -translation / maxTranslate;
                        deleteBg.setAlpha(factor);
                        deleteBg.setScaleX(0.85f + factor * 0.15f);
                        deleteBg.setScaleY(deleteBg.getScaleX());
                        return true;
                    }

                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // ✅ Fix: Close swipe if user scrolls or releases mid-way
                    if (isVertical[0] || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                        if (Math.abs(content.getTranslationX()) > 10f) {
                            closeSwipe(content, deleteBg);
                        }
                        if (v.getParent() != null)
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                        isOpen[0] = false;
                        currentlySwiped = null;
                        return false;
                    }

                    float totalDeltaX = event.getX() - downX[0];
                    float totalDeltaY = event.getY() - downY[0];

                    // Tap handling
                    if (Math.abs(totalDeltaX) < TAP_SLOP && Math.abs(totalDeltaY) < TAP_SLOP) {
                        if (isOpen[0]) {
                            closeSwipe(content, deleteBg);
                            isOpen[0] = false;
                            currentlySwiped = null;
                            return true;
                        } else {
                            v.performClick();
                            return false;
                        }
                    }

                    // ✅ Auto open / close logic
                    float progress = -totalDeltaX / v.getWidth();
                    if (progress > AUTO_OPEN_THRESHOLD) {
                        // Fully open
                        revealSwipe(content, deleteBg);
                        isOpen[0] = true;
                        currentlySwiped = content;
                        content.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        deleteBg.setOnClickListener(v1 -> {
                            if (onDeleteClick != null) onDeleteClick.run();
                            closeSwipe(content, deleteBg);
                            isOpen[0] = false;
                            currentlySwiped = null;
                        });
                    } else {
                        // Smoothly auto-close if stuck mid-way
                        if (Math.abs(content.getTranslationX()) > 10f) {
                            closeSwipe(content, deleteBg);
                        }
                        isOpen[0] = false;
                        currentlySwiped = null;
                    }

                    if (v.getParent() != null)
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return false;
        });
    }

    // Reveal animation
    private static void revealSwipe(View content, View bg) {
        if (content == null || bg == null) return;

        bg.setVisibility(View.VISIBLE);
        bg.setAlpha(0f);
        bg.setScaleX(0.85f);
        bg.setScaleY(0.85f);

        bg.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .start();

        content.animate()
                .translationX(-content.getWidth() * 0.25f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(220)
                .start();
    }

    // Close animation
    private static void closeSwipe(View content, View bg) {
        if (content == null || bg == null) return;

        content.animate()
                .translationX(0)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .setDuration(200)
                .start();

        bg.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> bg.setVisibility(View.GONE))
                .start();
    }
}
