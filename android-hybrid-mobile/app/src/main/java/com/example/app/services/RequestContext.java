package com.example.app.services;

import android.net.Uri;
import java.util.Map;
import java.util.HashMap;

public class RequestContext {
    private String method;
    private String path;
    private Uri uri;
    private byte[] body;
    private Map<String, String> headers;

    public RequestContext(String method, String path, Uri uri, byte[] body, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.uri = uri;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Uri getUri() { return uri; }
    public byte[] getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }

    public String getQueryParam(String name) {
        if (uri == null) return null;
        return uri.getQueryParameter(name);
    }

    public String getBodyAsString() {
        if (body == null || body.length == 0) return "";
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }
}
