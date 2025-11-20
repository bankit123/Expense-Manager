package trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyItem;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends BaseAdapter {

    private final Context context;
    private final OnCurrencySelectListener listener;

    private final List<CurrencyItem> fullList;  // All currencies
    private List<CurrencyItem> displayList;     // Filtered items
    private CurrencyItem selectedCurrency;      // ✅ Store selected item

    public interface OnCurrencySelectListener {
        void onSelect(CurrencyItem item);
    }

    public CurrencyAdapter(Context context, List<CurrencyItem> list, OnCurrencySelectListener listener) {
        this.context = context;
        this.listener = listener;
        this.fullList = new ArrayList<>(list);
        this.displayList = new ArrayList<>(list);
    }

    @Override
    public int getCount() {
        return displayList.size();
    }

    @Override
    public Object getItem(int position) {
        return displayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.mockup_item_currency_choice_card, parent, false);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        CurrencyItem item = displayList.get(position);

        h.tvCurrencySymbol.setText(item.symbol);
        h.tvCurrencyName.setText(item.currencyName + " (" + item.code + ")");

        // ✅ Maintain selection even during scroll
        boolean isSelected = selectedCurrency != null && selectedCurrency.equals(item);
        h.radioButton.setChecked(isSelected);

        // ✅ Change radio button tint color
        if (isSelected) {
            h.radioButton.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.nav_icon_active)
                    )
            );
        } else {
            h.radioButton.setButtonTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(context, R.color.gray)
                    )
            );
        }

        // ✅ Click on entire row or radio
        View.OnClickListener selectListener = v -> {
            selectedCurrency = item; // remember selected
            notifyDataSetChanged();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (listener != null) listener.onSelect(item);
        };

        convertView.setOnClickListener(selectListener);
        h.radioButton.setOnClickListener(selectListener);

        return convertView;
    }


    // ✅ Filter logic with reset
    public void filter(String query) {
        query = query.toLowerCase().trim();
        displayList.clear();

        if (query.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            for (CurrencyItem item : fullList) {
                if (item.currencyName.toLowerCase().contains(query)
                        || item.code.toLowerCase().contains(query)
                        || item.symbol.toLowerCase().contains(query)) {
                    displayList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public CurrencyItem getSelectedCurrency() {
        return selectedCurrency;
    }

    static class ViewHolder {
        TextView tvCurrencySymbol, tvCurrencyName;
        RadioButton radioButton;

        ViewHolder(View v) {
            tvCurrencySymbol = v.findViewById(R.id.tvCurrencySymbol);
            tvCurrencyName = v.findViewById(R.id.tvCurrencyName);
            radioButton = v.findViewById(R.id.rbSelect);
        }
    }
}
