package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import trackmyspend.budgetplanner.expensemanager.R;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(Calendar date);
    }

    private List<Calendar> days;
    private OnDayClickListener listener;
    private Calendar selectedDate;

    public DayAdapter(List<Calendar> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
        this.selectedDate = Calendar.getInstance(); // default today
    }

    public void setSelectedDate(Calendar date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Calendar date = days.get(position);
        String dayName = new SimpleDateFormat("EEE", Locale.getDefault()).format(date.getTime());
        int dayNumber = date.get(Calendar.DAY_OF_MONTH);

        holder.tvDayName.setText(dayName);
        holder.tvDayNumber.setText(String.valueOf(dayNumber));

        boolean isSelected = isSameDay(date, selectedDate);
        holder.itemView.setBackgroundResource(isSelected ?
                R.drawable.bg_selected_day : R.drawable.bg_unselected_day);

        holder.itemView.setOnClickListener(v -> {
            selectedDate = date;
            notifyDataSetChanged();
            listener.onDayClick(date);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvDayName;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayName = itemView.findViewById(R.id.tvDayName);
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
