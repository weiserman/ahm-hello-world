// =============================================================================
// AHM Hello World — Application Bootstrap (Vite Mode)
// =============================================================================
//
// WHAT THIS FILE DOES:
//   This is the main entry point that boots the Vue 3 application when running
//   in Vite mode (the standard production/development workflow).
//
// BOOTSTRAP SEQUENCE:
//   1. Import createApp from Vue 3 — the factory that creates a Vue app instance
//   2. Import the root component (Main.vue) — the shell that wraps all screens
//   3. Import the router — enables multi-screen navigation via URL hash routes
//   4. Import global styles — CSS design tokens consumed by all components
//   5. Create the app, install the router plugin, and mount to the DOM
//
// WHY `import { createApp } from 'vue'` WORKS:
//   - In Vite mode: Vite's dependency resolver maps 'vue' to the pre-bundled
//     Vue ESM module (either from node_modules or the vendored src/lib/vue/).
//   - In SFC-loader mode: The importmap in index.html maps 'vue' to the
//     vendored file directly (see main.sfc.js for that path).
//   The source code stays the same regardless of which resolver handles it.
//
// WHAT Main.vue PROVIDES AS THE ROOT COMPONENT:
//   Main.vue is a minimal shell containing a <router-view>. It provides the
//   structural wrapper (minimal-container) that all routed screen components
//   render inside of. In the production AHM framework, Main.vue also handles
//   global concerns like barcode scanner input and service worker registration.
//
// HOW THE ROUTER ENABLES MULTI-SCREEN NAVIGATION:
//   Vue Router intercepts URL changes (hash-based) and swaps the component
//   rendered inside <router-view> based on the current route. This gives us
//   a multi-screen app without full page reloads — essential for the native
//   app feel inside the Android WebView.
//
// CONNECTION TO THE AHM FRAMEWORK:
//   This bootstrap pattern mirrors the full AHM asset-delivery-scanner app:
//   createApp → use(router) → mount. The Hello World version is intentionally
//   minimal, but the architecture scales by adding more views, routes, and
//   global plugins (store, bridge services, etc.) without changing this file.
// =============================================================================

// createApp is Vue 3's application factory. It returns an app instance that
// can be configured with plugins (router, store, etc.) before mounting.
import { createApp } from 'vue';

// Main.vue is the root component — the outermost shell of the component tree.
// Every screen the user sees is rendered inside Main.vue's <router-view>.
import Main from './Main.vue';

// The router maps URL paths to Vue components. Importing it here gives us
// the configured router instance that we install as a Vue plugin below.
import router from './router/index.js';

// Global CSS with design tokens (colors, spacing, layout primitives).
// Imported as a side-effect — it injects styles into the page immediately.
import './style.css';

// --- Bootstrap sequence ---

// Step 1: Create the Vue application with Main as the root component.
// Main.vue's <router-view> will dynamically render child screen components.
const app = createApp(Main);

// Step 2: Install the router as a plugin. This:
//   - Makes <router-view> and <router-link> available in all templates
//   - Enables the useRoute() and useRouter() composables in all components
//   - Starts listening for URL hash changes to drive navigation
app.use(router);

// Step 3: Mount the app to the DOM. Vue takes over the <div id="app"> element
// and renders the component tree inside it. This must be called AFTER all
// plugins are installed — Vue warns if you mount before using plugins.
app.mount('#app');
