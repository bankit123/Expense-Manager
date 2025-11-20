package trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.R;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import java.util.Date;
import java.util.concurrent.Executors;

public class Name_Input_Fragment extends Fragment {

    public interface OnNameSavedListener {
        void onNameSaved();
    }

    private OnNameSavedListener callback;
    private AppDatabase db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNameSavedListener) {
            callback = (OnNameSavedListener) context;
        }
        db = Room.databaseBuilder(context, AppDatabase.class, "expense_manager_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_name__input, container, false);

        TextInputEditText etName = view.findViewById(R.id.etName);
        TextView btnSave = view.findViewById(R.id.btnSaveName);

        LinearLayout headerOnboarding = view.findViewById(R.id.headerOnboarding);
        LinearLayout headerProfile = view.findViewById(R.id.headerProfile);

        // ✅ Decide which header to show (default = onboarding)
        boolean fromProfile = getArguments() != null && getArguments().getBoolean("fromProfile", false);
        if (fromProfile) {
            headerProfile.setVisibility(View.VISIBLE);
            headerOnboarding.setVisibility(View.GONE);
        } else {
            headerOnboarding.setVisibility(View.VISIBLE);
            headerProfile.setVisibility(View.GONE);
        }

        // ✅ Save action
        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                etName.requestFocus();
                return;
            }
            saveName(name);
        });

        return view;
    }

    private void saveName(String name) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().getFirstUser();

            if (user != null) {
                user.name = name;
                user.updated_at = new Date();
                db.userDao().update(user);
            }

            if (callback != null) {
                requireActivity().runOnUiThread(callback::onNameSaved);
            }
        });
    }
}
