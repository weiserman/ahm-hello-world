package com.example.app.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import com.example.app.AppConfig;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServiceRegistry {
    private static final String TAG = "WebServiceRegistry";
    private final List<RouteMappingMetadata> routeMetadataList = new ArrayList<>();
    private final List<Object> domainControllers = new ArrayList<>();

    public WebServiceRegistry(Context context) {
        Log.d(TAG, "Initializing WebServiceRegistry...");
        // For Hello World: register known controllers explicitly
        registerController(new com.example.app.services.hello.HelloController());
        compileAllControllerRoutes();
    }

    private void registerController(Object controller) {
        domainControllers.add(controller);
    }

    private void compileAllControllerRoutes() {
        for (Object controller : domainControllers) {
            Method[] methods = controller.getClass().getMethods();
            Log.d(TAG, "Scanning controller: " + controller.getClass().getSimpleName());

            for (Method method : methods) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    String cleanPath = mapping.path();

                    // Extract path parameter token names (e.g. {id})
                    List<String> pathTokenNames = new ArrayList<>();
                    Matcher tokenMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(cleanPath);
                    while (tokenMatcher.find()) {
                        pathTokenNames.add(tokenMatcher.group(1));
                    }

                    // Compile the path into a regex pattern
                    String generalizedRegexPattern = cleanPath.replaceAll("\\{[^}]+\\}", "([^/]+)");
                    Pattern compiledRegex = Pattern.compile("^" + generalizedRegexPattern + "$");

                    routeMetadataList.add(new RouteMappingMetadata(
                            mapping.method().toUpperCase(),
                            cleanPath,
                            compiledRegex,
                            pathTokenNames,
                            method,
                            controller
                    ));
                    Log.d(TAG, "Compiled route: [" + mapping.method().toUpperCase() + "] " + cleanPath);
                }
            }
        }
    }

    /**
     * Dispatches an incoming WebView request to the matching controller method.
     *
     * @param context  The Android context
     * @param config   The app configuration
     * @param request  The original WebResourceRequest from the WebView
     * @param path     The request path (e.g. "/api/hello")
     * @param method   The HTTP method (e.g. "GET", "POST")
     * @return A WebResourceResponse with the controller's response, or null if no route matched
     */
    public WebResourceResponse dispatch(Context context, AppConfig config, WebResourceRequest request,
                                         String path, String method) {
        String cleanMethod = method.toUpperCase();
        String lookupPath = path;

        // Normalize trailing slash
        if (lookupPath.length() > 1 && lookupPath.endsWith("/")) {
            lookupPath = lookupPath.substring(0, lookupPath.length() - 1);
        }

        for (RouteMappingMetadata route : routeMetadataList) {
            if (route.httpMethod.equals(cleanMethod)) {
                Matcher matcher = route.regexPattern.matcher(lookupPath);
                if (matcher.matches()) {
                    try {
                        // Retrieve request body from AndroidBridge cache (if any)
                        byte[] body = AndroidBridge.getAndClearBody(method, path);

                        // Build headers map from the request
                        Map<String, String> headers = new HashMap<>();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Map<String, String> requestHeaders = request.getRequestHeaders();
                            if (requestHeaders != null) {
                                headers.putAll(requestHeaders);
                            }
                        }

                        // Create the RequestContext with the new constructor signature
                        RequestContext ctx = new RequestContext(
                                method, path, request.getUrl(), body, headers);

                        // Invoke the controller method via reflection
                        ResponseContext response = (ResponseContext) route.executionTarget.invoke(
                                route.instanceOwner, ctx);

                        if (response != null) {
                            return buildWebResourceResponse(response);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error invoking controller method: " + route.executionTarget.getName(), e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Converts a ResponseContext into a WebResourceResponse suitable for the WebView.
     */
    private WebResourceResponse buildWebResourceResponse(ResponseContext response) {
        String bodyStr = response.getBody();
        byte[] bodyBytes = bodyStr != null ? bodyStr.getBytes(StandardCharsets.UTF_8) : new byte[0];
        InputStream stream = new ByteArrayInputStream(bodyBytes);

        String contentType = response.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/json";
        }

        String reasonPhrase = getReasonPhrase(response.getStatusCode());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new WebResourceResponse(
                    contentType,
                    "UTF-8",
                    response.getStatusCode(),
                    reasonPhrase,
                    response.getHeaders(),
                    stream
            );
        } else {
            return new WebResourceResponse(contentType, "UTF-8", stream);
        }
    }

    /**
     * Maps common HTTP status codes to their reason phrases.
     */
    private String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default: return "OK";
        }
    }

    /**
     * Internal metadata structure holding compiled route information.
     */
    private static class RouteMappingMetadata {
        final String httpMethod;
        final String rawAnnotatedPath;
        final Pattern regexPattern;
        final List<String> pathTokenKeys;
        final Method executionTarget;
        final Object instanceOwner;

        RouteMappingMetadata(String httpMethod, String rawAnnotatedPath, Pattern regexPattern,
                              List<String> pathTokenKeys, Method executionTarget, Object instanceOwner) {
            this.httpMethod = httpMethod;
            this.rawAnnotatedPath = rawAnnotatedPath;
            this.regexPattern = regexPattern;
            this.pathTokenKeys = pathTokenKeys;
            this.executionTarget = executionTarget;
            this.instanceOwner = instanceOwner;
        }
    }
}
