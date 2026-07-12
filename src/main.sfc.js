// =============================================================================
// AHM Hello World — Application Bootstrap (SFC-Loader Mode)
// =============================================================================
//
// WHAT THIS FILE DOES:
//   This is the entry point for SFC-loader mode — an alternative way to boot
//   the Vue application that compiles .vue Single-File Components at runtime
//   inside the browser, WITHOUT a Vite build step.
//
// WHAT SFC-LOADER MODE IS:
//   Normally, Vue .vue files must be compiled by Vite (or Webpack) before the
//   browser can use them. SFC-loader mode uses the `vue3-sfc-loader` library
//   to compile .vue files on the fly in the browser. This means:
//     - You can edit .vue files and refresh the page — no rebuild needed
//     - The app runs from raw source files, not bundled output
//     - Useful for prototyping, debugging, and the Android WebView dev workflow
//
// WHY THIS IS OPTIONAL (PRODUCTION USES VITE):
//   SFC-loader mode is slower than pre-compiled Vite output because:
//     - Every .vue file is fetched and compiled on page load
//     - The vue3-sfc-parser library adds ~200KB to the initial download
//   In production, `vite build` pre-compiles everything into optimised bundles
//   that load instantly. This file (main.sfc.js) is only used when running
//   without a build step (e.g., opening index.html directly in a WebView).
//
// THE adoptedStyleSheets CSS LOADING APPROACH:
//   Instead of using a <link rel="stylesheet"> tag, we use the CSS Module
//   Scripts API (`import style from '...' with { type: 'css' }`) to load
//   the global stylesheet as a CSSStyleSheet object. We then add it to
//   `document.adoptedStyleSheets` — a modern API for applying stylesheets
//   without injecting <style> or <link> tags into the DOM.
//
//   Benefits:
//     - Works in SFC-loader mode where Vite's CSS injection isn't available
//     - The stylesheet is applied before any Vue component renders (no FOUC)
//     - Cleaner DOM — no extra <style> elements cluttering the <head>
//
// HOW bootstrapSfcApp() WORKS:
//   The function (defined in sfcBootstrap.js) dynamically loads Vue and
//   Vue Router from vendored files, defines components inline (since there's
//   no build step to compile .vue files), and returns { createApp, Main, router }.
//   We then mount the app exactly like main.js does in Vite mode.
// =============================================================================

// Import the bootstrap function that sets up Vue + Router in SFC-loader mode.
// This handles loading vendored Vue/Router libraries and defining components.
import { bootstrapSfcApp } from './util/sfcBootstrap.js';

// Import global CSS as a CSSStyleSheet object using the CSS Module Scripts API.
// The `with { type: 'css' }` syntax tells the browser to parse the import as CSS
// rather than JavaScript. This is a modern ESM feature (Chrome 93+, Firefox 108+).
import style from './style.css' with { type: 'css' };

// Apply the global stylesheet using adoptedStyleSheets.
// This appends our style.css to the document's adopted stylesheet array.
// adoptedStyleSheets are applied to the entire document, similar to <link> tags,
// but without creating DOM elements — they live in the CSSOM layer.
document.adoptedStyleSheets = [...document.adoptedStyleSheets, style];

// Bootstrap the SFC-loader app and mount it.
// bootstrapSfcApp() returns { createApp, Main, router } — the same three pieces
// that main.js gets from static imports. We then wire them up identically.
bootstrapSfcApp().then(({ createApp, Main, router }) => {
  // Create the Vue application with the root component
  const app = createApp(Main);

  // Install the router plugin (if one was created — SFC mode may skip it)
  if (router) app.use(router);

  // Mount to the same #app div used by Vite mode
  app.mount('#app');
}).catch(err => console.error('App initialization failed:', err));
