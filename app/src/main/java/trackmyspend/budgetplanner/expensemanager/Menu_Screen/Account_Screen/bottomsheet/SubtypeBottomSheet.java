package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.bottomsheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter.SubtypeBottomSheetAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Adapter.SubtypeListAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Add_Account_Activity;
import trackmyspend.budgetplanner.expensemanager.R;

public class SubtypeBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ACCOUNT_ID = "account_id";
    private long accountId;

    public static SubtypeBottomSheet newInstance(long accountId) {
        SubtypeBottomSheet fragment = new SubtypeBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getLong(ARG_ACCOUNT_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.bottomsheet_subtypes_account_details, container, false);

        LinearLayout btnAddSubtype = view.findViewById(R.id.btnAddSubtype);
        RecyclerView rv = view.findViewById(R.id.rvSubtypes);
        LinearLayout layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        ImageView closeBottomSheet = view.findViewById(R.id.closeBottomSheet);
        closeBottomSheet.setOnClickListener(v -> dismiss());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        AppDatabase db = AppDatabase.getDatabase(requireContext());

        db.subtypeDao().getSubtypesByAccountIdLive(accountId).observe(this, subtypes -> {

            if (subtypes == null || subtypes.isEmpty()) {
                rv.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                rv.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
                rv.setAdapter(new SubtypeBottomSheetAdapter(
                        subtypes,
                        subtype -> {
                            dismiss();
                        },
                        requireActivity()
                ));
            }

        });


        btnAddSubtype.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Add_Account_Activity.class);

            // Tell Add_Account_Activity to open in "Add Payment Method" mode
            intent.putExtra("mode", "add_subtype");

            // Pass current account ID
            intent.putExtra("accountId", accountId);

            startActivity(intent);
        });

        return view;
    }
}