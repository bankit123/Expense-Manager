package trackmyspend.budgetplanner.expensemanager.Profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Date;
import java.util.concurrent.Executors;

import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.ThemeManager;

public class DarkMode_Activity extends AppCompatActivity {

    LinearLayout optionSystem, optionLight, optionDark;
    ImageView tickSystem, tickLight, tickDark, ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 Load saved theme BEFORE UI loads
        String savedMode = ThemeManager.getTheme(this);
        ThemeManager.applyTheme(savedMode);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dark_mode);

        optionSystem = findViewById(R.id.optionSystem);
        optionLight = findViewById(R.id.optionLight);
        optionDark = findViewById(R.id.optionDark);

        tickSystem = findViewById(R.id.tickSystem);
        tickLight = findViewById(R.id.tickLight);
        tickDark = findViewById(R.id.tickDark);

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // ✔ Show correct tick
        updateTicks(savedMode);

        optionSystem.setOnClickListener(v -> setTheme("system"));
        optionLight.setOnClickListener(v -> setTheme("light"));
        optionDark.setOnClickListener(v -> setTheme("dark"));
    }

    private void setTheme(String mode) {

        // 1️⃣ Save to SharedPreferences
        ThemeManager.saveTheme(this, mode);

        // 2️⃣ Apply theme instantly
        ThemeManager.applyTheme(mode);

        // 3️⃣ Save to Room database also
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            db.userDao().updateMode(1, mode, new Date());
        });

        // 4️⃣ Update tick UI
        updateTicks(mode);

        // 5️⃣ Recreate to apply theme
        recreate();
    }

    private void updateTicks(String mode) {
        tickSystem.setVisibility(View.GONE);
        tickLight.setVisibility(View.GONE);
        tickDark.setVisibility(View.GONE);

        switch (mode) {
            case "system":
                tickSystem.setVisibility(View.VISIBLE);
                break;
            case "light":
                tickLight.setVisibility(View.VISIBLE);
                break;
            case "dark":
                tickDark.setVisibility(View.VISIBLE);
                break;
        }
    }
}
