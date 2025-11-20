package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen.Adapter;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;
import trackmyspend.budgetplanner.expensemanager.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class EMIAdapter extends RecyclerView.Adapter<EMIAdapter.EMIViewHolder> {

    public interface OnEmiClickListener {
        void onEmiClick(EMI emi);
    }

    private final OnEmiClickListener listener;
    private final List<EMI> emis = new ArrayList<>();
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public EMIAdapter(OnEmiClickListener listener) {
        this.listener = listener;
    }

    public void setEmis(List<EMI> newList) {
        emis.clear();
        if (newList != null) emis.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EMIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mockup_item_emi, parent, false);
        return new EMIViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EMIViewHolder h, int position) {
        EMI e = emis.get(position);

        h.loanName.setText(e.loan_name);
        h.lender.setText("Lender: " + e.lender_name);
        h.principal.setText("Principal: ₹" + e.principal_amount);
        h.interestRate.setText("Interest: " + e.interest_rate + "% p.a.");
        h.emiAmount.setText("EMI: ₹" + String.format(Locale.getDefault(), "%.2f", e.emi_amount));
        h.tenure.setText("Tenure: " + e.tenure_months + " months");
        h.paid.setText("Paid: " + e.paid_installments);

        if (e.start_date != null) h.startDate.setText("Start: " + df.format(e.start_date));
        if (e.end_date != null) h.endDate.setText("End: " + df.format(e.end_date));
        if (e.next_due_date != null) h.nextDue.setText("Next Due: " + df.format(e.next_due_date));

        h.status.setText("Status: " + e.status);
        if ("Active".equalsIgnoreCase(e.status)) {
            h.status.setTextColor(Color.parseColor("#388E3C"));
        } else if ("Completed".equalsIgnoreCase(e.status)) {
            h.status.setTextColor(Color.parseColor("#F57C00"));
        } else {
            h.status.setTextColor(Color.parseColor("#D32F2F"));
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onEmiClick(e); });
    }

    @Override
    public int getItemCount() { return emis.size(); }

    static class EMIViewHolder extends RecyclerView.ViewHolder {
        TextView loanName, lender, principal, interestRate, emiAmount,
                tenure, paid, startDate, endDate, nextDue, status;

        EMIViewHolder(View v) {
            super(v);
            loanName = v.findViewById(R.id.textLoanName);
            lender = v.findViewById(R.id.textLender);
            principal = v.findViewById(R.id.textPrincipal);
            interestRate = v.findViewById(R.id.textInterestRate);
            emiAmount = v.findViewById(R.id.textEmiAmount);
            tenure = v.findViewById(R.id.textTenure);
            paid = v.findViewById(R.id.textPaidInstallments);
            startDate = v.findViewById(R.id.textStartDate);
            endDate = v.findViewById(R.id.textEndDate);
            nextDue = v.findViewById(R.id.textNextDue);
            status = v.findViewById(R.id.textStatus);
        }
    }
}
