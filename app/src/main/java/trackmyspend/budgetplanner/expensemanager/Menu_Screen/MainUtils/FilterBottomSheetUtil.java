package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Set;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter.SubtypeListAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

/**
 * 🔹 A reusable utility to display a Transaction Filter BottomSheet
 * Works for Account, Home, or any other screen that needs filters
 */
public class FilterBottomSheetUtil {

    /** Callback when filters are applied or cleared */
    public interface OnFilterAppliedListener {
        void onApply(Set<String> selectedTypes, Set<Long> selectedSubtypeIds);
        void onClear();
    }

    // 🧩 Show bottom sheet
    public static void show(Context context,
                            long accountId,
                            Set<String> selectedTypes,
                            Set<Long> selectedSubtypeIds,
                            OnFilterAppliedListener listener) {

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_filter_bottom_sheet_accountdetails, null);
        dialog.setContentView(view);

        // --- UI References ---
        LinearLayout containerTransactionType = view.findViewById(R.id.containerTransactionType);
        LinearLayout layoutTypeOptions = view.findViewById(R.id.layoutTypeOptions);
        ImageView ivTypeDropdown = view.findViewById(R.id.ivTypeDropdown);

        LinearLayout containerSubtype = view.findViewById(R.id.containerSubtype);
        RecyclerView rvSubtypeList = view.findViewById(R.id.rvSubtypeList);
        ImageView ivSubtypeDropdown = view.findViewById(R.id.ivSubtypeDropdown);

        TextView tvIncome = view.findViewById(R.id.tvIncome);
        TextView tvExpense = view.findViewById(R.id.tvExpense);
        TextView tvTransfer = view.findViewById(R.id.tvTransfer);

        TextView btnApply = view.findViewById(R.id.btnApplyFilter);
        TextView btnClear = view.findViewById(R.id.btnClearFilter);
        ImageView ivCloseFilter = view.findViewById(R.id.ivCloseFilter);

        rvSubtypeList.setLayoutManager(new GridLayoutManager(context, 2));

        // --- Database ---
        AppDatabase db = AppDatabase.getDatabase(context);

        // --- Restore selections visually ---
        updateToggleUI(tvIncome, selectedTypes.contains("Income"));
        updateToggleUI(tvExpense, selectedTypes.contains("Expense"));
        updateToggleUI(tvTransfer,
                selectedTypes.contains("TransferCredit") || selectedTypes.contains("TransferDebit"));



        // --- Toggle logic ---
        tvIncome.setOnClickListener(v -> {
            toggleSelection(selectedTypes, "Income");
            updateToggleUI(tvIncome, selectedTypes.contains("Income"));
        });

        tvExpense.setOnClickListener(v -> {
            toggleSelection(selectedTypes, "Expense");
            updateToggleUI(tvExpense, selectedTypes.contains("Expense"));
        });

        tvTransfer.setOnClickListener(v -> {
            boolean currentlySelected = selectedTypes.contains("TransferCredit") || selectedTypes.contains("TransferDebit");

            if (currentlySelected) {
                selectedTypes.remove("TransferCredit");
                selectedTypes.remove("TransferDebit");
            } else {
                selectedTypes.add("TransferCredit");
                selectedTypes.add("TransferDebit");
            }

            updateToggleUI(tvTransfer, !currentlySelected);
        });



        // ---- Smooth Expand/Collapse Animation ----
        containerTransactionType.setOnClickListener(v -> toggleSection(layoutTypeOptions, ivTypeDropdown));
        containerSubtype.setOnClickListener(v -> toggleSection(rvSubtypeList, ivSubtypeDropdown));

        // ✅ Populate subtype list dynamically
        db.subtypeDao().getSubtypesByAccountIdLive(accountId).observeForever(subtypes -> {
            if (subtypes == null) return;

            SubtypeListAdapter adapter = new SubtypeListAdapter(context, subtypes, selectedSubtypeIds);
            rvSubtypeList.setAdapter(adapter);

            // --- Apply Filter ---
            btnApply.setOnClickListener(v -> {
                selectedSubtypeIds.clear();
                selectedSubtypeIds.addAll(adapter.getSelectedSubtypeIds());
                listener.onApply(selectedTypes, selectedSubtypeIds);
                dialog.dismiss();
            });

            // --- Clear Filter ---
            btnClear.setOnClickListener(v -> {
                selectedTypes.clear();
                selectedTypes.add("Income");
                selectedTypes.add("Expense");
                selectedTypes.add("TransferCredit");
                selectedTypes.add("TransferDebit");

                selectedSubtypeIds.clear();
                adapter.clearSelection();
                listener.onClear();
                dialog.dismiss();
            });
        });

        ivCloseFilter.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ----------------------------------------------------
    // 🔹 Helper Methods
    // ----------------------------------------------------
    private static void toggleSelection(Set<String> set, String value) {
        if (set.contains(value)) set.remove(value);
        else set.add(value);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void updateToggleUI(TextView view, boolean selected) {
        view.setBackgroundResource(selected
                ? R.drawable.bg_toggle_selected_filter
                : R.drawable.bg_toggle_unselected_filter);

        int color = view.getContext().getResources().getColor(
                selected ? R.color.nav_icon_active : R.color.opposite_color,
                view.getContext().getTheme());
        view.setTextColor(color);
    }

    private static void toggleSection(View section, ImageView arrow) {
        if (section.getVisibility() == View.VISIBLE) {
            collapse(section);
            arrow.animate().rotation(0).setDuration(200).start();
        } else {
            expand(section);
            arrow.animate().rotation(180).setDuration(200).start();
        }
    }

    private static void expand(View v) {
        v.measure(
                View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            v.getLayoutParams().height = animatedValue;
            v.requestLayout();
            v.setAlpha(animation.getAnimatedFraction());
        });
        animator.start();
    }

    private static void collapse(View v) {
        int initialHeight = v.getMeasuredHeight();
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            v.getLayoutParams().height = animatedValue;
            v.requestLayout();
            v.setAlpha(1f - animation.getAnimatedFraction());
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
}

