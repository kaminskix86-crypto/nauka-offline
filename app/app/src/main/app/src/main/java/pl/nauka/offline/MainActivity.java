package pl.nauka.offline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final int PICK_HTML_FILE = 1001;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#F8FAFC"));

        // Górny pasek z przyciskiem wyboru pliku
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#1E293B"));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dpToPx(10);
        topBar.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("📚 Strefa Nauki");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        topBar.addView(title, titleParams);

        Button openButton = new Button(this);
        openButton.setText("📂 Wczytaj zadanie");
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
        topBar.addView(openButton);

        rootLayout.addView(topBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Silnik wyświetlający zadania
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setBlockNetworkLoads(true); // Całkowita blokada ładowania z sieci

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Blokuje klikanie w jakiekolwiek zewnętrzne linki internetowe
                return true;
            }
        });

        rootLayout.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

        setContentView(rootLayout);
        showStartScreen();
    }

    private void showStartScreen() {
        String welcomeHtml = "<html><body style='font-family:sans-serif; text-align:center; padding:40px; color:#334155; background:#F8FAFC;'>"
                + "<h2>Gotowi do nauki! 🚀</h2>"
                + "<p style='font-size:16px; line-height:1.5;'>Kliknij przycisk <b>📂 Wczytaj zadanie</b> na górnym pasku i wybierz plik z dzisiejszymi zadaniami.</p>"
                + "</body></html>";
        webView.loadDataWithBaseURL("https://offline.local/", welcomeHtml, "text/html", "UTF-8", null);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_HTML_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_HTML_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                loadHtmlFromUri(uri);
            }
        }
    }

    private void loadHtmlFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            webView.loadDataWithBaseURL("https://offline.local/", builder.toString(), "text/html", "UTF-8", null);
        } catch (Exception e) {
            String errorHtml = "<html><body style='font-family:sans-serif; padding:20px; color:#DC2626;'>"
                    + "<h3>Nie udało się otworzyć pliku</h3><p>Upewnij się, że wybierasz plik z zadaniem (.html).</p></body></html>";
            webView.loadDataWithBaseURL("https://offline.local/", errorHtml, "text/html", "UTF-8", null);
        }
    }

    @Override
    public void onBackPressed() {
        // Zabezpieczenie przed przypadkowym zamknięciem quizu przyciskiem Wstecz
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }
}
