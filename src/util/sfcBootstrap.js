// =============================================================================
// AHM Hello World — SFC Bootstrap (Runtime Vue SFC Compilation)
// =============================================================================
//
// WHAT THIS FILE DOES:
//   Provides the bootstrapSfcApp() function that initializes a Vue 3 application
//   WITHOUT a build step. It loads Vue and Vue Router from vendored (checked-in)
//   ESM bundles and defines all components inline using defineComponent() with
//   string templates — bypassing the need for .vue file compilation.
//
// WHAT SFC-LOADER MODE IS:
//   In normal Vite mode, .vue files are compiled at build time into JavaScript
//   render functions. SFC-loader mode skips the build entirely — components are
//   defined as plain JavaScript objects with template strings that Vue compiles
//   at runtime. This is the "no build step" path through the AHM framework.
//
// THE MODULE CACHING STRATEGY (customJsCache):
//   Dynamic import() always returns a Promise, but the browser's module cache
//   means re-importing the same URL returns the same module. However, some
//   environments (older browsers, certain WebView implementations) may not
//   cache reliably. customJsCache provides an explicit in-memory cache:
//     - First call: import() the module and store the result
//     - Subsequent calls: return the cached result immediately
//   This ensures Vue and Router are only loaded once, even if bootstrapSfcApp()
//   is called multiple times (e.g., during hot reload or re-initialization).
//
// HOW IT DYNAMICALLY IMPORTS VUE AND VUE ROUTER:
//   Instead of static `import` statements (which Vite would resolve at build time),
//   we use dynamic `import()` with relative paths to the vendored ESM bundles.
//   The paths '../lib/vue/vue.esm-browser.prod.js' and
//   '../lib/vue-router/vue-router.esm-browser.js' point to the checked-in
//   library files that work directly in the browser without any bundler.
//
// WHY COMPONENTS ARE DEFINED INLINE:
//   In SFC-loader mode, there's no Vite or vue-loader to process .vue files.
//   Instead, components are defined using Vue's runtime API:
//     - defineComponent() creates a component from an options object
//     - The template is a string (not a .vue <template> block)
//     - The setup() function replaces <script setup>
//   This keeps the code self-contained — no .vue compilation step needed.
//
//   NOTE: The inline components here mirror the .vue files in the Vite-mode app.
//   Any changes to Home/index.vue or Main.vue should be replicated here for
//   SFC-loader mode to stay in sync.
//
// WHEN THIS MODE IS USEFUL VS. VITE MODE:
//   SFC-loader mode is useful when:
//     - You can't run a dev server (e.g., debugging inside Android WebView)
//     - You want to edit source files and refresh without rebuilding
//     - You're deploying to environments where only static files are available
//     - You want the simplest possible deployment (just copy source files)
//
//   Vite mode is better when:
//     - You want optimal performance (pre-compiled, tree-shaken, minified output)
//     - You're building for production deployment
//     - You want hot module replacement during development
//     - You're using TypeScript or other compile-to-JS languages
// =============================================================================

// Module cache: prevents redundant dynamic imports of the same library.
// Keys are URL strings; values are the resolved ES module objects.
const customJsCache = {};

// fetchModule(): Loads an ES module, using the cache to avoid duplicate loads.
// url: A relative path to a vendored ESM bundle (e.g., '../lib/vue/...').
// Returns: The resolved module object (with all its named exports).
async function fetchModule(url) {
  if (customJsCache[url]) return customJsCache[url];
  const module = await import(url);
  customJsCache[url] = module;
  return module;
}

// bootstrapSfcApp(): The main entry point for SFC-loader mode.
//
// This function:
//   1. Dynamically loads Vue 3 and Vue Router from vendored files
//   2. Extracts the needed APIs (createApp, defineComponent, ref, etc.)
//   3. Defines all components inline (mirroring the .vue files used in Vite mode)
//   4. Creates and configures the Vue Router with the same route definitions
//   5. Returns { createApp, Main, router } for the caller to mount
//
// The caller (main.sfc.js) then does:
//   const app = createApp(Main);
//   app.use(router);
//   app.mount('#app');
export async function bootstrapSfcApp() {
  // -----------------------------------------------------------------------
  // Step 1: Load Vue 3 and Vue Router from vendored ESM bundles.
  // These are the full browser builds — they include the compiler, runtime,
  // and all Composition API functions. No build tool needed.
  // -----------------------------------------------------------------------
  const vue = await fetchModule('../lib/vue/vue.esm-browser.prod.js');
  const vueRouter = await fetchModule('../lib/vue-router/vue-router.esm-browser.js');

  // Destructure the APIs we need — same functions that Vite mode gets via
  // static imports like `import { createApp } from 'vue'`.
  const { createApp, defineComponent, ref } = vue;
  const { createRouter, createWebHashHistory } = vueRouter;

  // -----------------------------------------------------------------------
  // Step 2: Define the Home component inline.
  // -----------------------------------------------------------------------
  // This mirrors src/views/home/index.vue from the Vite-mode app.
  // Instead of a .vue file with <template>/<script>/<style> blocks,
  // we use defineComponent() with:
  //   - template: A string containing the HTML (replaces <template>)
  //   - setup(): A function that returns reactive state (replaces <script setup>)
  //
  // NOTE: Scoped styles from the .vue version are NOT replicated here —
  // they are handled by the global style.css loaded in main.sfc.js.
  // -----------------------------------------------------------------------
  const Home = defineComponent({
    // Inline template string — same HTML as Home/index.vue's <template> block.
    // Vue compiles this to a render function at runtime.
    template: `
      <div class="app-layout">
        <header class="app-header"><h1>AHM Hello World</h1></header>
        <main class="app-content">
          <p class="badge">{{ greeting }}</p>
          <button class="action-button" @click="fetchGreeting">Call Native Bridge</button>
          <p v-if="nativeResponse" class="response-box">{{ nativeResponse }}</p>
        </main>
      </div>
    `,

    // setup() is the Composition API equivalent of <script setup>.
    // It runs once when the component is created and returns the reactive
    // state and methods that the template can access.
    setup() {
      // Reactive state — same as the ref() calls in the .vue version
      const greeting = ref('Hello from Vue 3!');
      const nativeResponse = ref('');

      // Bridge call — identical logic to the .vue version's fetchGreeting().
      // Calls /api/hello which is intercepted by the Android WebViewClient.
      async function fetchGreeting() {
        try {
          const res = await fetch('/api/hello');
          const data = await res.json();
          nativeResponse.value = data.message;
        } catch (err) {
          nativeResponse.value = 'Error: ' + err.message;
        }
      }

      // Return everything the template needs to access
      return { greeting, nativeResponse, fetchGreeting };
    }
  });

  // -----------------------------------------------------------------------
  // Step 3: Define the Main (root) component inline.
  // -----------------------------------------------------------------------
  // Mirrors src/Main.vue — a minimal shell with just a <router-view>.
  // No setup() needed because the root component has no state in Hello World.
  // -----------------------------------------------------------------------
  const Main = defineComponent({
    // The root template — just a container wrapping the router outlet
    template: '<div class="minimal-container"><router-view></router-view></div>'
  });

  // -----------------------------------------------------------------------
  // Step 4: Configure the router with the same routes as Vite mode.
  // -----------------------------------------------------------------------
  // Route definitions are identical to src/router/index.js.
  // Hash history mode for file:// protocol compatibility.
  // -----------------------------------------------------------------------
  const routes = [
    { path: '/', redirect: '/home' },
    { name: 'home', path: '/home', component: Home }
  ];

  const router = createRouter({
    history: createWebHashHistory(),
    routes
  });

  // -----------------------------------------------------------------------
  // Step 5: Return everything the caller needs to mount the app.
  // -----------------------------------------------------------------------
  // The caller (main.sfc.js) receives these and wires them up:
  //   createApp(Main).use(router).mount('#app')
  // -----------------------------------------------------------------------
  return { createApp, Main, router };
}
