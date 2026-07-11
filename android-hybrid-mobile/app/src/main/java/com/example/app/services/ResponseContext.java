package com.example.app.services;

import java.util.HashMap;
import java.util.Map;

public class ResponseContext {
    private int statusCode;
    private String contentType;
    private String body;
    private Map<String, String> headers;

    private ResponseContext() {
        this.headers = new HashMap<>();
    }

    public static Builder status(int code) {
        return new Builder(code);
    }

    public int getStatusCode() { return statusCode; }
    public String getContentType() { return contentType; }
    public String getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }

    public static class Builder {
        private ResponseContext response;

        Builder(int statusCode) {
            response = new ResponseContext();
            response.statusCode = statusCode;
        }

        public Builder contentType(String type) {
            response.contentType = type;
            return this;
        }

        public Builder header(String name, String value) {
            response.headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            response.body = body;
            return this;
        }

        public ResponseContext build() {
            return response;
        }
    }
}
