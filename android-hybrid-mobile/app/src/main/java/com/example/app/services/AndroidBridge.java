package com.example.app.services;

import android.util.Log;
import android.webkit.JavascriptInterface;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidBridge {
    private static final String TAG = "AndroidBridge";
    private static final ConcurrentHashMap<String, String> bodyMapCache = new ConcurrentHashMap<>();

    @JavascriptInterface
    public void captureRequestBody(String method, String urlPath, String bodyContent, String jsTimestampStr) {
        if (method == null || urlPath == null || bodyContent == null) return;
        String lookupKey = method.toUpperCase() + ":" + cleanPathString(urlPath);
        bodyMapCache.put(lookupKey, bodyContent);
        Log.d(TAG, "Captured body for: " + lookupKey);
    }

    public static byte[] getAndClearBody(String method, String urlPath) {
        if (method == null || urlPath == null) return new byte[0];
        String lookupKey = method.toUpperCase() + ":" + cleanPathString(urlPath);
        String match = bodyMapCache.remove(lookupKey);
        return match != null ? match.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    private static String cleanPathString(String path) {
        if (path == null) return "";
        // Remove query string and fragment
        int queryIdx = path.indexOf('?');
        if (queryIdx >= 0) path = path.substring(0, queryIdx);
        int fragIdx = path.indexOf('#');
        if (fragIdx >= 0) path = path.substring(0, fragIdx);
        return path;
    }
}
