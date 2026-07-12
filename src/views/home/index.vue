<!--
  ===========================================================================
  AHM Hello World — Home View (Main Screen)
  ===========================================================================

  WHAT THIS FILE IS:
    The primary screen component of the Hello World app. It demonstrates the
    core AHM pattern of calling the native Android bridge from Vue via a
    simple fetch() call.

  TEMPLATE STRUCTURE:
    .app-layout        → Flexbox column layout filling the full viewport
      .app-header      → Fixed top bar with the app title (see style.css)
      .app-content     → Scrollable content area below the header
        .badge         → Displays the static greeting text
        .action-button → Triggers the native bridge call on click
        .response-box  → Shows the native response (conditionally rendered)

  HOW fetchGreeting() CALLS THE NATIVE ANDROID BRIDGE:
    The function calls `fetch('/api/hello')`. In a normal web app, this would
    make an HTTP request to a server. But inside the AHM Android WebView:

    1. The Android app registers a WebViewClient with shouldInterceptRequest()
    2. When the WebView sees a request to `/api/hello`, it intercepts it
    3. Instead of making a network call, the Java bridge code handles it:
       - Reads native device data, sensor info, or any Android API
       - Returns a JSON response as if it were a real HTTP server
    4. The Vue code receives a normal Response object — no special API needed

    This is the AHM "native bridge" pattern: the web app talks to the native
    layer through standard web APIs (fetch), and the Android WebViewClient
    intercepts and fulfills those requests natively. The web code never needs
    to know it's not talking to a real server.

  WHY /api/hello IS NOT A REAL NETWORK CALL:
    In the Android WebView, WebResourceRequest interception happens at the
    WebViewClient level before any network I/O. The Java code in
    ConfigWebViewClient.java or a service handler constructs a WebResourceResponse
    with the JSON payload. This means:
    - No internet connection required
    - No CORS issues
    - Sub-millisecond response times
    - The same fetch() API works in browser testing (if a mock server is running)

  HOW REACTIVE REFS WORK:
    ref() creates a reactive wrapper around a primitive value.
    - `const greeting = ref('Hello')` creates a reactive reference
    - Reading: `greeting.value` returns the current string
    - Writing: `greeting.value = 'New'` triggers Vue to re-render any
      template that references `greeting`
    - In templates, you don't need .value — Vue auto-unwraps refs in templates
      (that's why the template says {{ greeting }} not {{ greeting.value }})

  ERROR HANDLING IN THE FETCH CALL:
    The try/catch ensures the app doesn't crash if the bridge call fails.
    Common failure modes:
    - Running in a regular browser (no WebViewClient to intercept)
    - The Android bridge service throws an exception
    - Network timeout (if somehow a real HTTP request is made)
    On error, we display the error message in the response box so the
    developer can diagnose the issue.

  CSS VARIABLES USED:
    - -/-accent-color: The green highlight color (#42b883), defined in :root
      in style.css. Used for the action button background.
    - -/-surface-color: The card/panel background (#1a1a1e), also from :root.
      Used for the response box to visually distinguish it from the page bg.

  HOW SCOPED STYLES WORK:
    The `scoped` attribute on <style> tells Vue to add a unique data attribute
    (e.g., data-v-abc123) to all elements in this component's template, and
    scope all CSS selectors to only match elements with that attribute.
    This prevents style leakage — .action-button here won't affect buttons
    in other components.
  ===========================================================================
-->

<template>
  <!--
    app-layout: The standard AHM layout shell (defined in style.css).
    Provides a full-viewport flexbox column with fixed header and scrollable content.
  -->
  <div class="app-layout">
    <!--
      app-header: Fixed-position top bar.
      Stays pinned to the top while content scrolls beneath it.
      Height is controlled by the -/-header-height CSS variable.
    -->
    <header class="app-header">
      <h1>AHM Hello World</h1>
    </header>

    <!--
      app-content: Scrollable main area.
      margin-top pushes content below the fixed header.
      flex-direction: column + align-items: center centers children horizontally.
    -->
    <main class="app-content">
      <!-- badge: A small label styled via the global .badge class in style.css -->
      <p class="badge">{{ greeting }}</p>

      <!--
        action-button: Triggers the native bridge call.
        @click is Vue's shorthand for v-on:click — binds the fetchGreeting
        function to the button's click event.
      -->
      <button class="action-button" @click="fetchGreeting">
        Call Native Bridge
      </button>

      <!--
        response-box: Conditionally rendered only when nativeResponse has a value.
        v-if removes the element from the DOM entirely when the condition is false
        (unlike v-show which just hides it with display:none).
      -->
      <p v-if="nativeResponse" class="response-box">
        {{ nativeResponse }}
      </p>
    </main>
  </div>
</template>

<script setup>
// ---------------------------------------------------------------------------
// Script Setup — Vue 3 Composition API
// ---------------------------------------------------------------------------
// <script setup> is a compile-time syntactic sugar for the Composition API.
// Everything declared here is automatically exposed to the template.
// No need for a separate `return` statement or `export default` block.
// ---------------------------------------------------------------------------

import { ref } from 'vue';

// Reactive references for UI state.
// ref() wraps a primitive value in a reactive proxy. When .value changes,
// Vue re-renders any template bindings that depend on it.
const greeting = ref('Hello from Vue 3!');
const nativeResponse = ref('');

// fetchGreeting(): Calls the native Android bridge via a standard fetch().
//
// THE AHM BRIDGE PATTERN:
//   In the full AHM framework, fetch() calls to specific URL patterns
//   (like /api/*) are intercepted by the Android WebViewClient's
//   shouldInterceptRequest() method. The Java layer handles the request
//   and returns a WebResourceResponse — the web code just sees a normal
//   fetch() Promise that resolves with JSON.
//
//   This means the SAME Vue code works:
//     - In the Android WebView (intercepted by native bridge)
//     - In a desktop browser with a mock server (for development)
//     - In unit tests with a mocked fetch()
async function fetchGreeting() {
  try {
    const res = await fetch('/api/example/get-test?tracking_id=hello-world&filter=demo');
    const data = await res.json();
    nativeResponse.value = 'Native replied: ' + JSON.stringify(data);
  } catch (err) {
    nativeResponse.value = 'Error: ' + err.message;
  }
}
</script>

<style scoped>
/*
 * Scoped styles for the Home view.
 * These selectors only apply to elements within THIS component's template
 * thanks to Vue's scoped CSS mechanism (adds data-v-* attributes automatically).
 *
 * CSS variables (--accent-color, --surface-color) are inherited from :root
 * in style.css — they cascade down to all components.
 */

/* Action button: Accent-colored call-to-action for the bridge call */
.action-button {
  background-color: var(--accent-color); /* Green (#42b883) from design tokens */
  color: #121214;                        /* Dark text for contrast on green */
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
}

/* Response box: Surface-colored panel displaying the native bridge response */
.response-box {
  background-color: var(--surface-color); /* Dark panel (#1a1a1e) from tokens */
  padding: 1rem;
  border-radius: 6px;
  width: 100%;
  text-align: center;
}
</style>
