// =============================================================================
// AHM Hello World — Global Reactive Store with localStorage Persistence
// =============================================================================
//
// WHAT THIS FILE DOES:
//   Creates a single, globally-shared reactive state object (the "store") that
//   automatically persists every change to the browser's localStorage. This gives
//   the app durable state that survives page reloads and WebView restarts.
//
// THE REACTIVE() + LOCALSTORAGE PERSISTENCE PATTERN:
//   1. We define a default state shape (defaultState) with initial values.
//   2. On first load, we attempt to read previously saved state from localStorage.
//   3. We wrap the merged state in Vue's reactive() to make it observable.
//   4. We deep-watch the entire store and serialize it to localStorage on every change.
//
//   This creates a "self-saving" store — any component that mutates
//   `store.user.name = 'Alice'` triggers the watcher, which saves to disk.
//
// WHY window.__AHM_HELLO_STORE__ IS USED (SINGLETON PATTERN):
//   In JavaScript module systems, the same module can sometimes be imported
//   multiple times through different paths (especially with dynamic imports or
//   SFC-loader). By attaching the store to `window`, we guarantee:
//     - Only ONE store instance exists, regardless of how many times this
//       module is imported or re-evaluated.
//     - The store survives hot module replacement during development.
//     - Both Vite mode and SFC-loader mode share the same global reference.
//
//   The double-underscore prefix (__AHM_HELLO_STORE__) is a convention to
//   avoid collisions with other libraries and signal "internal use."
//
// THE getInitialState() PATTERN:
//   Merges saved state with defaults using spread syntax:
//     { ...defaultState, ...parsed }
//   This ensures that if we add new fields to defaultState in a future version,
//   they appear even for users who already have saved state (without migration).
//   Top-level keys from saved state override defaults; new keys fall through.
//
// HOW DEEP WATCHING WORKS:
//   watch() with { deep: true } recursively watches all nested properties.
//   Without it, only top-level property replacements would trigger the callback.
//   With it, changes like `store.config.greeting = 'Hi'` are detected too.
//
//   CAVEAT: Deep watching is expensive for large state trees. In the production
//   AHM app (asset-delivery-scanner), the store uses the same pattern but with
//   a larger state shape. For very large stores, consider debouncing the save.
//
// CONNECTION TO AHM'S STORE PATTERN:
//   The full AHM asset-delivery-scanner uses the identical pattern:
//     window.__GLOBAL_APP_STORE__ = reactive(getInitialState())
//     + deep watch → localStorage.setItem()
//   The only differences are the storage key name and the state shape.
//   This Hello World version demonstrates the same architecture at minimal scale.
// =============================================================================

import { reactive, watch } from 'vue';

// The localStorage key under which the entire store is serialized.
// Using a unique key prevents collisions with other apps on the same origin.
const STORAGE_KEY = 'ahm_hello_world_store';

// Default state shape — defines all available fields and their initial values.
// When adding new state, add it here so it's always defined even on first load.
const defaultState = {
  user: { name: 'Developer', isLoggedIn: false },
  config: {
    greeting: 'Hello World'
  }
};

// getInitialState(): Attempts to restore saved state from localStorage.
// Falls back to defaults if:
//   - No saved state exists (first run)
//   - The saved JSON is corrupted (storage tampering or disk error)
// The spread merge ensures new defaultState fields appear alongside saved data.
const getInitialState = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) return { ...defaultState };
    const parsed = JSON.parse(saved);
    // Merge: saved values override defaults, but new defaults still appear
    return { ...defaultState, ...parsed };
  } catch (error) {
    // If JSON parsing fails, start fresh with defaults
    return { ...defaultState };
  }
};

// Singleton guard: Only create the reactive store if it doesn't already exist.
// This prevents duplicate stores if this module is re-imported (HMR, SFC-loader).
if (!window.__AHM_HELLO_STORE__) {
  // Create a Vue reactive proxy around the initial state.
  // Any reads are tracked by Vue's reactivity system; any writes trigger watchers.
  window.__AHM_HELLO_STORE__ = reactive(getInitialState());

  // Deep watcher: Serialize the entire store to localStorage on ANY change.
  // { deep: true } ensures nested property mutations (e.g., store.user.name)
  // also trigger this callback, not just top-level reassignments.
  watch(() => window.__AHM_HELLO_STORE__, (newState) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newState));
  }, { deep: true });
}

// Export the singleton store for use in Vue components.
// Components import this as: import { store } from '@/util/store'
// Then read/write: store.user.name, store.config.greeting, etc.
export const store = window.__AHM_HELLO_STORE__;
