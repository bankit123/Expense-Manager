package trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.ColorEntity;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Icon;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter.ColorAdapter;
import trackmyspend.budgetplanner.expensemanager.Menu_Screen.MainUtils.Category.Adapter.IconAdapter;
import trackmyspend.budgetplanner.expensemanager.R;

import java.util.List;
import java.util.concurrent.Executors;

public class CategoryIconColorPickerUtil {

    public interface OnSelectionComplete {
        void onSelected(String iconName, String drawableName, String colorHex);
    }

    public static void setupIconAndColorPicker(Context context,
                                               FrameLayout iconContainer,
                                               ImageView ivCategoryIcon,
                                               RecyclerView rvIcons,
                                               RecyclerView rvColors,
                                               boolean isExpense,
                                               OnSelectionComplete callback) {

        AppDatabase db = AppDatabase.getDatabase(context);

        rvIcons.setLayoutManager(new GridLayoutManager(context, 8));
        rvColors.setLayoutManager(new GridLayoutManager(context, 8));

        final String[] selectedIconName = {null};
        final String[] selectedDrawableName = {null};
        final String[] selectedColorHex = {null};

        Executors.newSingleThreadExecutor().execute(() -> {
            String type = isExpense ? "Expense" : "Income";
            List<Icon> icons = db.iconDao().getIconsByType(type);
            List<ColorEntity> colors = db.colorDao().getAllColors();

            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
            mainHandler.post(() -> {
                // ICONS
                IconAdapter iconAdapter = new IconAdapter(context, icons, selectedIcon -> {
                    int resId = context.getResources().getIdentifier(
                            selectedIcon.drawableName, "drawable", context.getPackageName());
                    ivCategoryIcon.setImageResource(resId);
                    ivCategoryIcon.setVisibility(android.view.View.VISIBLE);

                    selectedIconName[0] = selectedIcon.displayName;
                    selectedDrawableName[0] = selectedIcon.drawableName;

                    // ✅ Fire callback only if both icon + color chosen
                    if (selectedDrawableName[0] != null && selectedColorHex[0] != null) {
                        callback.onSelected(selectedIconName[0], selectedDrawableName[0], selectedColorHex[0]);
                    }
                });
                rvIcons.setAdapter(iconAdapter);

                // COLORS
                ColorAdapter colorAdapter = new ColorAdapter(colors, selectedColor -> {
                    int colorInt = Color.parseColor(selectedColor.hex);
                    GradientDrawable bgShape = new GradientDrawable();
                    bgShape.setShape(GradientDrawable.RECTANGLE);
                    bgShape.setCornerRadius(
                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics())
                    );
                    bgShape.setColor(ContextCompat.getColor(context, R.color.bg_card));
                    bgShape.setTint(colorInt);

                    iconContainer.setBackground(bgShape);


                    selectedColorHex[0] = selectedColor.hex;

                    // ✅ Fire callback only if both icon + color chosen
                    if (selectedDrawableName[0] != null && selectedColorHex[0] != null) {
                        callback.onSelected(selectedIconName[0], selectedDrawableName[0], selectedColorHex[0]);
                    }
                });
                rvColors.setAdapter(colorAdapter);
            });
        });
    }
}