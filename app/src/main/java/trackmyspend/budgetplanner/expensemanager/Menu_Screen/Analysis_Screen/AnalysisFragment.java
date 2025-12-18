package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Analysis_Screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.Graph.CategorySummary;
import trackmyspend.budgetplanner.expensemanager.DB.Graph.CustomPieChartView;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Analysis_Screen.Adapter.CategorySummaryAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.FilterUtil;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyFormatterUtil;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalysisFragment extends Fragment {

    RecyclerView recyclerCategorySummary;
    LinearLayout layoutEmptyState, noGraphPreview;

    private TextView tvSelectedFilter, tvTransactionSubtitle;
    private View dropdownContainer;
    private TextView tvWeekly, tvMonthly, tvYearly, tvPeriod;

    private CustomPieChartView customPieChart;
    private String currentType = "Expense"; // ✅ default is Expense
    private Date startDate, endDate;
    private CategorySummaryAdapter summaryAdapter;

    private AppDatabase db;

    public AnalysisFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        tvSelectedFilter = view.findViewById(R.id.tvSelectedFilter);
        tvTransactionSubtitle = view.findViewById(R.id.tvTransactionSubtitle);
        dropdownContainer = view.findViewById(R.id.dropdownContainer);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        noGraphPreview = view.findViewById(R.id.noGraphPreview);

        FrameLayout bannerContainer = view.findViewById(R.id.banner_container);
        PriorityBannerController.show(
                requireActivity(),
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );

        tvWeekly = view.findViewById(R.id.tvWeekly);
        tvMonthly = view.findViewById(R.id.tvMonthly);
        tvYearly = view.findViewById(R.id.tvYearly);
        tvPeriod = view.findViewById(R.id.tvPeriod);

        customPieChart = view.findViewById(R.id.customPieChart);

        db = AppDatabase.getDatabase(requireContext());

        // ✅ show "Expense" as default label
        tvSelectedFilter.setText(currentType);

        setupCustomDropdown();
        setupTimeFilters();

        // ✅ Setup RecyclerView
        setupCategoryRecycler(view);

        return view;
    }

    private void setupCategoryRecycler(View root) {
        recyclerCategorySummary = root.findViewById(R.id.recyclerCategorySummary);
        summaryAdapter = new CategorySummaryAdapter();
        recyclerCategorySummary.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCategorySummary.setAdapter(summaryAdapter);
    }

    // 🔹 Dropdown Income / Expense
    private void setupCustomDropdown() {
        dropdownContainer.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popupMenu =
                    new androidx.appcompat.widget.PopupMenu(requireContext(), dropdownContainer);
            popupMenu.getMenu().add("Income");
            popupMenu.getMenu().add("Expense");

            popupMenu.setOnMenuItemClickListener(item -> {
                currentType = item.getTitle().toString();
                tvSelectedFilter.setText(currentType);
                loadCategoryChart();
                return true;
            });

            popupMenu.show();
        });
    }

    // 🔹 Time filters (using FilterUtil)
    private void setupTimeFilters() {
        setFilterSelected(tvWeekly, tvMonthly, tvYearly, tvPeriod);

        // Default → Weekly
        FilterUtil.getWeeklyRange((start, end, label) -> {
            startDate = start;
            endDate = end;
            tvTransactionSubtitle.setText(label);
            loadCategoryChart();
        });

        tvWeekly.setOnClickListener(v -> {
            setFilterSelected(tvWeekly, tvMonthly, tvYearly, tvPeriod);
            FilterUtil.getWeeklyRange((start, end, label) -> {
                startDate = start;
                endDate = end;
                tvTransactionSubtitle.setText(label);
                loadCategoryChart();
            });
        });

        tvMonthly.setOnClickListener(v -> {
            setFilterSelected(tvMonthly, tvWeekly, tvYearly, tvPeriod);
            FilterUtil.getMonthlyRange((start, end, label) -> {
                startDate = start;
                endDate = end;
                tvTransactionSubtitle.setText(label);
                loadCategoryChart();
            });
        });

        tvYearly.setOnClickListener(v -> {
            setFilterSelected(tvYearly, tvWeekly, tvMonthly, tvPeriod);
            FilterUtil.getYearlyRange((start, end, label) -> {
                startDate = start;
                endDate = end;
                tvTransactionSubtitle.setText(label);
                loadCategoryChart();
            });
        });

        tvPeriod.setOnClickListener(v -> {
            setFilterSelected(tvPeriod, tvWeekly, tvMonthly, tvYearly);
            FilterUtil.showPeriodPicker(requireContext(), getParentFragmentManager(),
                    (start, end, label) -> {
                        startDate = start;
                        endDate = end;
                        tvTransactionSubtitle.setText(label);
                        loadCategoryChart();
                    });
        });
    }

    // 🔹 Highlight selected filter
    private void setFilterSelected(TextView selected, TextView... others) {
        int activeColor = requireContext().getResources().getColor(R.color.nav_icon_active);
        int defaultColor = requireContext().getResources().getColor(R.color.nav_icon_default);

        selected.setBackgroundResource(R.drawable.bg_segment_selected);
        selected.setTextColor(activeColor);

        for (TextView tv : others) {
            tv.setBackgroundResource(R.drawable.bg_segment_unselected);
            tv.setTextColor(defaultColor);
        }
    }

    // 🔹 Load Category Chart (unchanged)
    private void loadCategoryChart() {
        if (startDate == null || endDate == null) return;

        db.transactionDao().getCategorySummary(1, currentType, startDate, endDate) // userId = 1
                .observe(getViewLifecycleOwner(), summaries -> {
                    if (summaries == null || summaries.isEmpty()) {
                        customPieChart.setSlices(new ArrayList<>(), currentType + " " + CurrencyFormatterUtil.format(0));
                        customPieChart.setVisibility(View.GONE);
                        recyclerCategorySummary.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        noGraphPreview.setVisibility(View.VISIBLE);
                        return;
                    }

                    noGraphPreview.setVisibility(View.GONE);
                    customPieChart.setVisibility(View.VISIBLE);
                    recyclerCategorySummary.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);

                    List<CustomPieChartView.Slice> slices = new ArrayList<>();
                    float totalAmount = 0f;

                    for (CategorySummary summary : summaries) {
                        String cat = summary.categoryName != null ? summary.categoryName : "Other";
                        String color = (summary.colorHex != null && !summary.colorHex.isEmpty())
                                ? summary.colorHex : "#BDBDBD"; // default gray
                        slices.add(new CustomPieChartView.Slice(cat, (float) summary.totalAmount, color));
                        totalAmount += summary.totalAmount;
                    }

                    // ✅ Use CurrencyFormatterUtil for center text
                    String center = currentType + " " + CurrencyFormatterUtil.format(totalAmount);
                    customPieChart.setSlices(slices, center);

                    // ✅ Update RecyclerView
                    summaryAdapter.setData(summaries);
                });
    }
}
