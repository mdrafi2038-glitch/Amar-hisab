package com.hisab.app;

import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.print.PrintJob;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WebView printWebView;
    private final ArrayList<PrintJob> printJobs = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);

        webView.addJavascriptInterface(new PrintBridge(), "AndroidPrint");

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://") ||
                    url.startsWith("file://") || url.startsWith("about:")) return false;
                try {
                    startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                        "এই অ্যাপটি এই ডিভাইসে ইনস্টল করা নেই।", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        webView.loadUrl("https://appassets.androidplatform.net/assets/hisab.html");
    }

    private class PrintBridge {
        @JavascriptInterface
        public void print(String html) {
            runOnUiThread(() -> startPrint(html));
        }
    }

    private void startPrint(String html) {
        try {
            printWebView = new WebView(this);
            WebSettings s = printWebView.getSettings();
            s.setJavaScriptEnabled(false);
            s.setDomStorageEnabled(false);
            printWebView.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    createPrintJob(view);
                }
            });
            printWebView.loadDataWithBaseURL(
                "https://appassets.androidplatform.net/",
                html,
                "text/html",
                "UTF-8",
                null
            );
        } catch (Exception e) {
            Toast.makeText(this, "প্রিন্ট প্রস্তুত করা যায়নি।", Toast.LENGTH_LONG).show();
        }
    }

    private void createPrintJob(WebView view) {
        try {
            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
            if (pm == null) {
                Toast.makeText(this, "এই ফোনে Android Print Service নেই।", Toast.LENGTH_LONG).show();
                return;
            }

            String jobName = "আজকের হিসাব";
            PrintAttributes attributes = new PrintAttributes.Builder().build();
            PrintJob job = pm.print(
                jobName,
                view.createPrintDocumentAdapter(jobName),
                attributes
            );
            printJobs.add(job);
        } catch (Exception e) {
            Toast.makeText(this, "প্রিন্ট সার্ভিস চালু করা যায়নি।", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
