package com.example.app;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.example.app.services.WebServiceRegistry;
import com.example.app.services.WebScripts;

class MyWebViewClient extends WebViewClient {
    private static final String TAG = "MyWebViewClient";
    private final Context mContext;
    private final AppConfig mConfig;
    private final WebServiceRegistry mServiceRegistry;

    public MyWebViewClient(Context context, AppConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mServiceRegistry = new WebServiceRegistry(context);
    }

    @Override
    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(WebScripts.INTERCEPT_SCRIPT, null);
        } else {
            view.loadUrl("javascript:" + WebScripts.INTERCEPT_SCRIPT);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
        String path = uri.getPath();

        // 1. Route /api/* paths to WebServiceRegistry
        if (path != null && path.startsWith("/api/")) {
            String method = request.getMethod();
            WebResourceResponse serviceResponse = mServiceRegistry.dispatch(mContext, mConfig, request, path, method);
            if (serviceResponse != null) {
                return serviceResponse;
            }
            // Fallback error if no controller matched
            String errorJson = "{\"status\":\"error\",\"message\":\"Native API route not found.\"}";
            InputStream fallbackStream = new ByteArrayInputStream(errorJson.getBytes(StandardCharsets.UTF_8));
            return new WebResourceResponse("application/json", "UTF-8", fallbackStream);
        }

        // 2. Serve virtual host assets from assets/www/
        String targetHost = uri.getHost();
        String rawVirtualHost = getRawVirtualHost();

        if (targetHost != null && targetHost.equals(rawVirtualHost)) {
            if (path != null) {
                String assetPath = path;
                if (assetPath.startsWith("/")) {
                    assetPath = assetPath.substring(1);
                }
                if (assetPath.isEmpty()) {
                    assetPath = "index.html";
                }
                try {
                    InputStream stream = resolveAssetStream(assetPath);
                    String mimeType = getMimeType(assetPath);
                    return new WebResourceResponse(mimeType, "UTF-8", stream);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading asset: " + assetPath + " - " + e.getMessage());
                    String errorHtml = "<html><body><h2>Application Error</h2>"
                            + "<p>Resource could not be loaded from assets.</p></body></html>";
                    InputStream fallbackStream = new ByteArrayInputStream(errorHtml.getBytes(StandardCharsets.UTF_8));
                    return new WebResourceResponse("text/html", "UTF-8", fallbackStream);
                }
            }
        }

        // 3. Pass through everything else
        return super.shouldInterceptRequest(view, request);
    }

    private String getRawVirtualHost() {
        if (mConfig == null || mConfig.getVirtualHost().isEmpty()) return null;
        return Uri.parse(mConfig.getVirtualHost()).getHost();
    }

    /**
     * Resolves an asset stream from the bundled assets/www/ directory.
     */
    private InputStream resolveAssetStream(String relativePath) throws IOException {
        return mContext.getAssets().open("www/" + relativePath);
    }

    /**
     * Returns the MIME type for common web file extensions.
     */
    private String getMimeType(String path) {
        if (path.contains("?")) path = path.split("\\?")[0];
        if (path.contains("#")) path = path.split("#")[0];
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".ttf")) return "font/ttf";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (view == null || request == null || error == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int errorCode = error.getErrorCode();
            String targetUrl = request.getUrl().toString();
            if (request.isForMainFrame()) {
                Log.e(TAG, "Error [" + errorCode + "] for: " + targetUrl);
                if (!targetUrl.contains("error.html") && mConfig != null) {
                    view.loadUrl(mConfig.getVirtualHost() + "/error.html");
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (view != null && failingUrl != null && !failingUrl.contains("error.html")) {
            Log.e(TAG, "Legacy error: " + description);
            if (mConfig != null) {
                view.loadUrl(mConfig.getVirtualHost() + "/error.html");
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
        String failingUrl = (error != null) ? error.getUrl() : "Unknown URL";
        if (Build.VERSION.SDK_INT <= 30) {
            Log.w(TAG, "Overriding SSL error for: " + failingUrl);
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
}
