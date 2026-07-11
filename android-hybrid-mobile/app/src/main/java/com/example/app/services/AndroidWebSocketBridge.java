package com.example.app.services;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.util.Log;

public class AndroidWebSocketBridge {
    private static final String TAG = "AndroidWebSocketBridge";
    private Activity mActivity;
    private WebView mWebView;

    public AndroidWebSocketBridge(Activity activity, WebView webView) {
        this.mActivity = activity;
        this.mWebView = webView;
    }

    @JavascriptInterface
    public void connectNative(String sessionId, String path) {
        Log.d(TAG, "WebSocket connect requested: " + path + " (session: " + sessionId + ")");
        // Stub: WebSocket support can be added later
    }

    @JavascriptInterface
    public void sendNative(String sessionId, String message) {
        Log.d(TAG, "WebSocket send: " + sessionId + " -> " + message);
    }

    @JavascriptInterface
    public void closeNative(String sessionId) {
        Log.d(TAG, "WebSocket close: " + sessionId);
    }
}
