package com.example.app.services.hello;

import android.util.Log;
import com.example.app.services.RequestContext;
import com.example.app.services.RequestMapping;
import com.example.app.services.ResponseContext;
import org.json.JSONObject;

public class HelloController {
    private static final String TAG = "HelloController";

    public HelloController() {}

    @RequestMapping(path = "/api/hello", method = "GET")
    public ResponseContext getGreeting(RequestContext request) {
        try {
            String name = request.getQueryParam("name");
            if (name == null || name.isEmpty()) {
                name = "World";
            }

            JSONObject result = new JSONObject();
            result.put("status", "ok");
            result.put("message", "Hello, " + name + " from Android Native!");
            result.put("timestamp", System.currentTimeMillis());

            Log.i(TAG, "Served greeting to: " + name);

            return ResponseContext.status(200)
                    .contentType("application/json")
                    .header("X-Processed-By", "HelloController")
                    .body(result.toString())
                    .build();
        } catch (Exception e) {
            JSONObject err = new JSONObject();
            try {
                err.put("status", "error");
                err.put("message", e.getMessage());
            } catch (Exception ignored) {}
            return ResponseContext.status(500)
                    .contentType("application/json")
                    .body(err.toString())
                    .build();
        }
    }
}
