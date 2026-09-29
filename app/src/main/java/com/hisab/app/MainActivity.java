package com.hisab.app;

import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("about:")) return false;
                try { startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME)); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "এই অ্যাপটি এই ডিভাইসে ইনস্টল করা নেই।", Toast.LENGTH_LONG).show(); }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/hisab.html");
    }
    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
    private void doPrint() {
        PrintManager pm = (PrintManager)getSystemService(PRINT_SERVICE);
        pm.print("আজকের হিসাব", webView.createPrintDocumentAdapter("আজকের হিসাব"), new PrintAttributes.Builder().build());
    }
}