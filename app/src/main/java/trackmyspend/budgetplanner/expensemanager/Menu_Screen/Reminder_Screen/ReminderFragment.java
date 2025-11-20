package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.*;
import android.widget.*;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen.Adapter.EMIAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.List;

public class ReminderFragment extends Fragment {

    private EMIAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEmi);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EMIAdapter(emi -> {
            Intent i = new Intent(getContext(), EmiDetailsActivity.class);
            i.putExtra("emi", emi);
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        Button btnAdd = view.findViewById(R.id.btnAddEmi);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddEmiActivity.class)));

        loadEmis();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadEmis();
    }

    private void loadEmis() {
        new Thread(() -> {
            List<EMI> emis = AppDatabase.getDatabase(requireContext()).emiDao().getActiveEMIsSync();
            requireActivity().runOnUiThread(() -> adapter.setEmis(emis));
        }).start();
    }


}
