// =============================================================================
// AHM Hello World — Router Configuration
// =============================================================================
//
// WHAT THIS FILE DOES:
//   Configures Vue Router — the client-side navigation system that maps URL
//   paths to Vue components. This enables a multi-screen single-page app
//   without full page reloads.
//
// WHY createWebHashHistory() IS USED:
//   Vue Router supports two history modes:
//     1. createWebHistory()     — uses the browser's History API (clean URLs like /home)
//     2. createWebHashHistory() — uses the URL hash (URLs like #/home)
//
//   We use HASH mode because:
//     - The app runs inside an Android WebView loading from file:///android_asset/www/
//     - Hash-based routing works with the file:// protocol — no web server needed
//     - The fragment identifier (after #) is never sent to the server, so it works
//       identically whether served from a CDN, a local file, or a WebView asset
//     - No server-side URL rewriting / fallback configuration is required
//
// HOW ROUTE-TO-COMPONENT MAPPING WORKS:
//   Each route object has:
//     - path: The URL fragment (after #) that triggers this route
//     - component: The Vue component to render when this route is active
//     - name (optional): A friendly identifier for programmatic navigation
//     - redirect (optional): Automatically navigates to another path
//
//   When the URL changes (e.g., user clicks a link or we call router.push()),
//   Vue Router finds the matching route and renders its component inside
//   <router-view> in Main.vue.
//
// HOW TO ADD MORE ROUTES (EXTENSION PATTERN):
//   1. Create a new view: src/views/my-feature/index.vue
//   2. Import it here: import MyFeature from '../views/my-feature/index.js'
//   3. Add a route: { name: 'my-feature', path: '/my-feature', component: MyFeature }
//   4. Navigate to it: <router-link to="/my-feature"> or router.push('/my-feature')
//
// WHY THERE ARE NO ROUTE GUARDS:
//   Route guards (beforeEach, beforeResolve) are used in production apps for:
//     - Authentication checks (redirect to login if not authenticated)
//     - Permission verification
//     - Data pre-fetching before navigation
//   This Hello World app has no auth or permissions, so guards aren't needed.
//   The production AHM app uses guards to check PIN authentication state.
// =============================================================================

import { createRouter, createWebHashHistory } from 'vue-router';

// Import the Home view component.
// Convention: each view lives in src/views/<feature>/index.vue and is imported
// directly here — no barrel files or index.js re-exports needed.
import Home from '../views/home/index.vue';

// Route definitions — the mapping from URL paths to Vue components.
const routes = [
  // Redirect: When the user lands on the root URL (#/), automatically navigate
  // to /home. This ensures there's always a meaningful screen displayed.
  { path: '/', redirect: '/home' },

  // Home route: Renders the Home component when the URL is #/home.
  // The `name` property enables programmatic navigation via:
  //   router.push({ name: 'home' })
  { name: 'home', path: '/home', component: Home }
];

// Create the router instance.
// This is the singleton router that gets installed into the Vue app via app.use(router).
const router = createRouter({
  // Hash history mode — works with file:// protocol and Android WebView assets.
  // URLs will look like: file:///android_asset/www/index.html#/home
  history: createWebHashHistory(),
  routes
});

// Export the configured router so main.js can install it as a Vue plugin.
export default router;
