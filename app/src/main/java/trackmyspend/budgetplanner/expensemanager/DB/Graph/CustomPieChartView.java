package trackmyspend.budgetplanner.expensemanager.DB.Graph;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomPieChartView extends View {

    private Paint slicePaint;
    private Paint textPaint;
    private Paint linePaint;
    private RectF rectF;
    private List<Slice> slices = new ArrayList<>();

    private float totalValue = 0f;
    private float chartRadius;
    private String centerText = "";

    private float animationProgress = 1f;
    private float rotationAngle = -90f;
    private float lastTouchAngle;

    private int selectedSliceIndex = -1;
    private float selectionShift = 0f;

    public CustomPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.LEFT);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(3f);

        rectF = new RectF();

        // ✅ Apply theme colors based on Dark/Light Mode
        int nightModeFlags = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark Mode
            textPaint.setColor(Color.WHITE);
            linePaint.setColor(Color.LTGRAY);
        } else {
            // Light Mode
            textPaint.setColor(Color.BLACK);
            linePaint.setColor(Color.DKGRAY);
        }
    }

    public void setSlices(List<Slice> slices, String centerText) {
        this.slices = slices;
        this.centerText = centerText;
        this.totalValue = 0f;
        for (Slice s : slices) totalValue += s.value;

        startAnimation();
    }

    private void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animationProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices == null || slices.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();

        // 🔹 Dynamic chart radius
        int baseRadius = Math.min(width, height) / 2 - 100;
        if (slices.size() > 5) baseRadius -= 40;
        if (slices.size() > 8) baseRadius -= 80;
        if (slices.size() > 12) baseRadius -= 120;

        chartRadius = baseRadius;
        rectF.set(width / 2f - chartRadius, height / 2f - chartRadius,
                width / 2f + chartRadius, height / 2f + chartRadius);

        float startAngle = rotationAngle;

        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            float sweep = (s.value / totalValue) * 360f * animationProgress;

            // Slice color
            try {
                slicePaint.setColor(Color.parseColor(s.colorHex));
            } catch (Exception e) {
                slicePaint.setColor(Color.GRAY);
            }

            // 🔹 Offset selected slice outward
            float offsetX = 0, offsetY = 0;
            if (i == selectedSliceIndex) {
                float angle = (float) Math.toRadians(startAngle + sweep / 2f);
                offsetX = (float) Math.cos(angle) * selectionShift;
                offsetY = (float) Math.sin(angle) * selectionShift;
            }

            RectF sliceRect = new RectF(rectF);
            sliceRect.offset(offsetX, offsetY);
            canvas.drawArc(sliceRect, startAngle, sweep, true, slicePaint);

            if (animationProgress == 1f) {
                float angle = (float) Math.toRadians(startAngle + sweep / 2f);
                float lineStartX = (float) (width / 2 + Math.cos(angle) * (chartRadius * 0.9));
                float lineStartY = (float) (height / 2 + Math.sin(angle) * (chartRadius * 0.9));
                float lineEndX = (float) (width / 2 + Math.cos(angle) * (chartRadius + 80));
                float lineEndY = (float) (height / 2 + Math.sin(angle) * (chartRadius + 80));

                canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint);

                // 🔹 Auto-scale text size
                if (slices.size() > 10) {
                    textPaint.setTextSize(22f);
                } else if (slices.size() > 6) {
                    textPaint.setTextSize(26f);
                } else {
                    textPaint.setTextSize(30f);
                }

                String label = s.category + " " +
                        String.format(Locale.getDefault(), "%.0f%%", (s.value / totalValue) * 100f);

                float textWidth = textPaint.measureText(label);

                // 🔹 Prevent text going out of screen
                if (lineEndX < textWidth) {
                    lineEndX = textWidth + 20;
                }
                if (lineEndX + textWidth > width) {
                    lineEndX = width - textWidth - 20;
                }

                if (lineEndX < width / 2) {
                    textPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(label, lineEndX - 10, lineEndY, textPaint);
                } else {
                    textPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(label, lineEndX + 10, lineEndY, textPaint);
                }
            }

            startAngle += sweep;
        }

        // 🔹 Hole (center)
        Paint holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int nightModeFlags = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            holePaint.setColor(Color.BLACK);   // Dark Mode center
        } else {
            holePaint.setColor(Color.WHITE);   // Light Mode center
        }
        canvas.drawCircle(width / 2f, height / 2f, chartRadius * 0.55f, holePaint);

        // 🔹 Center text
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(38f);
        canvas.drawText(centerText, width / 2f, height / 2f, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - getWidth() / 2f;
        float y = event.getY() - getHeight() / 2f;
        float touchAngle = (float) Math.toDegrees(Math.atan2(y, x));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchAngle = touchAngle;
                return true;

            case MotionEvent.ACTION_MOVE:
                float angleDiff = touchAngle - lastTouchAngle;
                rotationAngle += angleDiff;
                lastTouchAngle = touchAngle;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                detectSliceTap(x, y);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void detectSliceTap(float x, float y) {
        float dist = (float) Math.sqrt(x * x + y * y);
        if (dist > chartRadius) return;

        float angle = (float) Math.toDegrees(Math.atan2(y, x)) - rotationAngle;
        if (angle < 0) angle += 360f;

        float start = 0f;
        for (int i = 0; i < slices.size(); i++) {
            float sweep = (slices.get(i).value / totalValue) * 360f;
            if (angle >= start && angle <= start + sweep) {
                animateSelection(i);
                break;
            }
            start += sweep;
        }
    }

    private void animateSelection(int index) {
        if (selectedSliceIndex == index) {
            selectedSliceIndex = -1;
            invalidate();
            return;
        }
        selectedSliceIndex = index;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 40f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            selectionShift = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public static class Slice {
        public String category;
        public float value;
        public String colorHex;

        public Slice(String category, float value, String colorHex) {
            this.category = category;
            this.value = value;
            this.colorHex = colorHex;
        }
    }
}
