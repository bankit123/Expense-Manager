package trackmyspend.budgetplanner.expensemanager.Profile;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import trackmyspend.budgetplanner.expensemanager.R;

public class Privacy_Policy_Activity extends AppCompatActivity {

    ImageView ivBack;
    WebView webViewPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        ivBack = findViewById(R.id.ivBack);
        webViewPrivacy = findViewById(R.id.webViewPrivacy);

        ivBack.setOnClickListener(v -> finish());

        // ✅ Configure WebView
        WebSettings webSettings = webViewPrivacy.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webViewPrivacy.setWebViewClient(new WebViewClient());

        // ✅ Load HTML file from res/raw
        String htmlData = readHtmlFromRaw(R.raw.privacy_policy);
        webViewPrivacy.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

    // Helper method to read the HTML file from /res/raw
    private String readHtmlFromRaw(int resourceId) {
        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder htmlBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlBuilder.toString();
    }
}
