// =============================================================================
// AHM Hello World — Vite Build Configuration
// =============================================================================
//
// This file configures Vite, the build tool used for:
//   1. Development server with hot module reload (vite dev)
//   2. Production builds that bundle everything into static dist/ output (vite build)
//
// AHM CONTEXT:
//   The production build output is copied into android/assets/www/ so that the
//   Android WebView can load the entire Vue SPA as local files — no server needed.
//   This drives several design decisions below.
//
// TWO ENTRY POINTS EXIST IN THIS PROJECT:
//   - index.html       → SFC-loader mode (no build step, compiles .vue in browser)
//   - index.vite.html  → Vite mode (pre-compiled at build time, used for production)
//
// We use index.vite.html as the Vite entry so that index.html remains untouched
// and can serve as the standalone SFC-loader entry point for quick prototyping.
// =============================================================================

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import fs from 'fs';
import path from 'path';

export default defineConfig({
  plugins: [
    // ---------------------------------------------------------------------------
    // @vitejs/plugin-vue
    // Teaches Vite how to compile .vue Single-File Components.
    // Without this, Vite would not know how to handle <template>, <script setup>,
    // or <style scoped> blocks inside .vue files.
    // ---------------------------------------------------------------------------
    vue(),

    // ---------------------------------------------------------------------------
    // Plugin: dev-server-spa-fallback
    // ---------------------------------------------------------------------------
    // WHY THIS EXISTS:
    //   By default, Vite's dev server serves `index.html` when you visit `/`.
    //   But our Vite entry point is `index.vite.html` (we keep index.html reserved
    //   for the SFC-loader mode). Without this middleware, visiting http://localhost:5173/
    //   would serve the wrong file — the SFC-loader version, not the Vite version.
    //
    // WHAT IT DOES:
    //   Intercepts requests for `/` or `/index.html` and rewrites them internally
    //   to `/index.vite.html` so the correct entry point is served during development.
    //
    // HOW IT WORKS:
    //   configureServer() gives us access to Connect-style middleware.
    //   We mutate req.url before Vite's own static-file handler sees it.
    //   Calling next() passes control to the rest of the middleware chain.
    // ---------------------------------------------------------------------------
    {
      name: 'dev-server-spa-fallback',
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          if (req.url === '/' || req.url === '/index.html') {
            req.url = '/index.vite.html';
          }
          next();
        });
      }
    },

    // ---------------------------------------------------------------------------
    // Plugin: rename-vite-html-output
    // ---------------------------------------------------------------------------
    // WHY THIS EXISTS:
    //   Vite uses the input filename as the output filename. Since our build input
    //   is `index.vite.html`, the production build would produce `dist/index.vite.html`.
    //   But the Android WebView (and web servers) expect the entry file to be named
    //   `index.html`. This plugin renames it after the bundle completes.
    //
    // WHEN IT RUNS:
    //   closeBundle() fires after Vite has finished writing all output files.
    //   This is the safest lifecycle hook for post-build file operations.
    //
    // RESULT:
    //   dist/index.vite.html → dist/index.html
    //   The dist/ folder can then be copied directly into android/assets/www/.
    // ---------------------------------------------------------------------------
    {
      name: 'rename-vite-html-output',
      closeBundle: async () => {
        const buildDir = resolve(__dirname, 'dist');
        const oldPath = path.join(buildDir, 'index.vite.html');
        const newPath = path.join(buildDir, 'index.html');
        if (fs.existsSync(oldPath)) {
          await fs.promises.rename(oldPath, newPath);
          console.log('Successfully renamed build output to standard index.html');
        }
      }
    }
  ],

  // ---------------------------------------------------------------------------
  // Dev server options
  // ---------------------------------------------------------------------------
  // `open` auto-opens the browser to the correct Vite entry point on `vite dev`.
  // ---------------------------------------------------------------------------
  server: {
    open: '/index.vite.html'
  },

  // ---------------------------------------------------------------------------
  // Production build options
  // ---------------------------------------------------------------------------
  // rollupOptions.input tells Rollup (Vite's underlying bundler) which HTML file
  // is the entry point. Vite parses this HTML file, finds all <script> and <link>
  // tags, and builds the dependency graph from there.
  //
  // We point to index.vite.html rather than index.html so that:
  //   - index.html stays clean for SFC-loader mode
  //   - The Vite build output is fully self-contained
  // ---------------------------------------------------------------------------
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.vite.html')
      }
    }
  }
});
