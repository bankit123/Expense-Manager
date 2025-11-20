package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Account_Subtype;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.Account_Screen.Add_Account_Activity;
import trackmyspend.budgetplanner.expensemanager.R;

public class SubtypePickerUtil {

    public interface OnSubtypeSelected {
        void onSelected(Subtype subtype);
    }

    public static void showSubtypePicker(Context context,
                                         ImageView ivSubtypeIcon,
                                         TextView tvSubtype,
                                         String titleText,
                                         OnSubtypeSelected callback) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_subtype, null);
        dialog.setContentView(sheetView);

        ImageView closeBottomSheet = sheetView.findViewById(R.id.closeBottomSheet);
        closeBottomSheet.setOnClickListener(v -> dialog.dismiss());

        // ✅ Set dynamic title
        TextView titleView = sheetView.findViewById(R.id.tvPickerTitle);
        if (titleView != null) {
            titleView.setText(titleText);
        }

        LinearLayout btnAddSubtype = sheetView.findViewById(R.id.btnAddSubtype);
        RecyclerView rvSubtypes = sheetView.findViewById(R.id.rvSubtypes);

        // Open Add Account
        btnAddSubtype.setOnClickListener(v -> {
            Intent intent = new Intent(context, Add_Account_Activity.class);
            context.startActivity(intent);
            dialog.dismiss();
        });

        // DB
        AppDatabase db = AppDatabase.getDatabase(context);

        // Observe Accounts + Subtypes
        db.accountDao().getAllAccountsLive().observe((LifecycleOwner) context, accounts -> {
            db.subtypeDao().getAllSubtypesLive().observe((LifecycleOwner) context, subtypes -> {
                if (accounts == null || subtypes == null) return;

                List<Object> combinedList = new ArrayList<>();

                // Combine Account + Subtypes
                for (Account account : accounts) {
                    combinedList.add(account);
                    for (Subtype subtype : subtypes) {
                        if (subtype.account_id == account.account_id) {
                            combinedList.add(subtype);
                        }
                    }
                }

                // Grid layout
                GridLayoutManager layoutManager = new GridLayoutManager(context, 3);

                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        Object item = combinedList.get(position);
                        return (item instanceof Account) ? 3 : 1;
                    }
                });

                rvSubtypes.setLayoutManager(layoutManager);

                // Adapter
                rvSubtypes.setAdapter(new SubtypePickerGroupedAdapter(context, combinedList, subtype -> {
                    int resId = context.getResources().getIdentifier(subtype.icon, "drawable", context.getPackageName());
                    ivSubtypeIcon.setImageResource(resId);
                    tvSubtype.setText(subtype.name);

                    callback.onSelected(subtype);
                    dialog.dismiss();
                }));
            });
        });

        dialog.show();
    }
}
