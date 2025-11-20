package trackmyspend.budgetplanner.expensemanager.Util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import trackmyspend.budgetplanner.expensemanager.R;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);


        TextView tvMessage = findViewById(R.id.tvUpdateMessage);
        Button btnUpdate = findViewById(R.id.btnUpdateNow);

        tvMessage.setText("A new version of TrackMySpend is available. Please update to continue.");

        btnUpdate.setOnClickListener(v -> {
            // 🔗 Replace with your Play Store link
            String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });
    }
}