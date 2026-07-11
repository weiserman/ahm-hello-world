package com.example.app.services;

public class WebScripts {
    /**
     * JavaScript that overrides window.fetch to capture request bodies for
     * POST/PUT/PATCH/DELETE requests before the original fetch runs.
     * The captured body is passed to AndroidBridge.captureRequestBody() so
     * the Java-side interceptor can retrieve it when handling the request.
     *
     * Injected via webView.evaluateJavascript() in MyWebViewClient.onPageStarted().
     */
    public static final String INTERCEPT_SCRIPT =
        "(function() {\n" +
        "    if (window.HasAndroidFetchIntercepted) return;\n" +
        "    window.HasAndroidFetchIntercepted = true;\n" +
        "    \n" +
        "    const originalFetch = window.fetch;\n" +
        "    window.fetch = function(input, init) {\n" +
        "        let targetUrl = '';\n" +
        "        let requestConfig = init || {};\n" +
        "        \n" +
        "        if (typeof input === 'string') {\n" +
        "            targetUrl = input;\n" +
        "        } else if (input instanceof URL) {\n" +
        "            targetUrl = input.href;\n" +
        "        } else if (input && typeof input.url === 'string') {\n" +
        "            targetUrl = input.url;\n" +
        "            if (!init) { requestConfig = input; }\n" +
        "        }\n" +
        "        \n" +
        "        const method = (requestConfig.method || 'GET').toUpperCase();\n" +
        "        const validMutations = ['POST', 'PUT', 'PATCH', 'DELETE'];\n" +
        "        \n" +
        "        if (requestConfig.body && validMutations.includes(method)) {\n" +
        "            let payloadString = typeof requestConfig.body === 'string'\n" +
        "                ? requestConfig.body\n" +
        "                : JSON.stringify(requestConfig.body);\n" +
        "            \n" +
        "            if (window.AndroidBridge && typeof window.AndroidBridge.captureRequestBody === 'function') {\n" +
        "                window.AndroidBridge.captureRequestBody(method, targetUrl, payloadString, String(Date.now()));\n" +
        "            }\n" +
        "        }\n" +
        "        return originalFetch.apply(this, arguments);\n" +
        "    };\n" +
        "    \n" +
        "    const originalOpen = XMLHttpRequest.prototype.open;\n" +
        "    XMLHttpRequest.prototype.open = function(method, url) {\n" +
        "        this._method = method ? method.toUpperCase() : 'GET';\n" +
        "        this._url = url;\n" +
        "        return originalOpen.apply(this, arguments);\n" +
        "    };\n" +
        "    \n" +
        "    const originalSend = XMLHttpRequest.prototype.send;\n" +
        "    XMLHttpRequest.prototype.send = function(body) {\n" +
        "        const validMutations = ['POST', 'PUT', 'PATCH', 'DELETE'];\n" +
        "        if (body && validMutations.includes(this._method) && typeof this._url === 'string') {\n" +
        "            let payloadString = typeof body === 'string' ? body : JSON.stringify(body);\n" +
        "            if (window.AndroidBridge && typeof window.AndroidBridge.captureRequestBody === 'function') {\n" +
        "                window.AndroidBridge.captureRequestBody(this._method, this._url, payloadString, String(Date.now()));\n" +
        "            }\n" +
        "        }\n" +
        "        return originalSend.apply(this, arguments);\n" +
        "    };\n" +
        "})();";
}
