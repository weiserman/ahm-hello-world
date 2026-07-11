package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "JS_CONSOLE_JAVA_MainActivity";
    private WebView mWebView;
    private AppConfig mConfig;
    private StorageManager mStorageManager;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConfig = new AppConfig(this);
        mStorageManager = new StorageManager(this, mConfig);

        if (mConfig.getVirtualHost().isEmpty()) return;

        mStorageManager.createPublicWorkspaceDirectory();

        mWebView = findViewById(R.id.activity_main_webview);
        configureWebViewSettings(mWebView);

        mWebView.addJavascriptInterface(
            new com.example.app.services.AndroidBridge(), "AndroidBridge");
        mWebView.addJavascriptInterface(
            new com.example.app.services.AndroidWebSocketBridge(this, mWebView),
            "AndroidWebSocketBridge");

        mWebView.setWebViewClient(new MyWebViewClient(this, mConfig));
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                Log.d("JS_CONSOLE",
                    String.format("[%s] %s:%d %s",
                        msg.messageLevel(),
                        msg.sourceId(),
                        msg.lineNumber(),
                        msg.message()));
                return true;
            }
        });

        String startupPath = mStorageManager.determineStartupPath();
        mWebView.loadUrl(mConfig.getVirtualHost() + startupPath);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebViewSettings(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT <= 30) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
