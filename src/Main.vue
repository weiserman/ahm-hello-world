<!--
  ===========================================================================
  AHM Hello World — Root Component (Main.vue)
  ===========================================================================

  WHAT THIS FILE IS:
    The root component of the Vue application. Every screen the user sees
    is rendered inside this component's <router-view> slot.

  WHY <router-view> IS THE CORE OF THE ROOT COMPONENT:
    <router-view> is a dynamic placeholder provided by Vue Router. It
    automatically renders whichever component matches the current URL route.
    When the route changes (e.g., from /home to /settings), <router-view>
    swaps the rendered component without a full page reload — giving the
    app a native-like navigation experience inside the Android WebView.

    Think of Main.vue as the "picture frame" and <router-view> as the
    "canvas" — the frame stays fixed while the content changes.

  COMPARISON TO THE PRODUCTION AHM Main.vue:
    In the full AHM asset-delivery-scanner, Main.vue is much more complex:
    - It creates a hidden <input> for hardware barcode scanner wedge input
    - It sets up a setInterval to keep focus on the scanner input
    - It listens for CustomEvents from the native Android bridge
    - It registers the Service Worker for offline support
    - It includes the top navigation bar component (<MenuTop />)

    This Hello World version strips all that away to show the minimal
    viable root component: just a container and a router outlet.

  WHY THE SCRIPT SETUP BLOCK IS EMPTY:
    The root component doesn't need any reactive state, composables, or
    lifecycle hooks. All global state (the store) is managed in store.js
    and consumed directly by child views. In the production version, this
    is where barcode scanner logic and global event listeners would live.
  ===========================================================================
-->

<template>
  <!--
    minimal-container: The outermost wrapper for the entire app.
    All routed screen components render inside <router-view> below.
    The CSS class is defined in the <style> block at the bottom of this file.
  -->
  <div class="minimal-container">
    <!--
      router-view: Vue Router's dynamic outlet.
      Renders the component that matches the current route.
      When the URL hash changes (e.g., #/home → #/about), this
      automatically destroys the old component and mounts the new one.
    -->
    <router-view></router-view>
  </div>
</template>

<script setup>
// ---------------------------------------------------------------------------
// Empty script setup block.
// ---------------------------------------------------------------------------
// The root component intentionally has no logic in this Hello World version.
// In a more complex app, this is where you would:
//   - Initialize global state (store)
//   - Set up hardware input listeners (barcode scanner)
//   - Register the Service Worker
//   - Define global event handlers
//
// Child views access the store directly via `import { store } from '@/util/store'`
// rather than receiving it through props from this root component.
</script>

<style>
/*
 * minimal-container styles
 * ---------------------------------------------------------------------------
 * width: 100% ensures the container spans the full viewport width.
 * This is important inside the Android WebView where the WebView itself
 * might not fill the screen — the container ensures our content does.
 *
 * box-sizing: border-box makes padding and border included in the width
 * calculation, preventing horizontal overflow when padding is added.
 *
 * NOTE: This uses unscoped <style> (no `scoped` attribute) because
 * minimal-container is a global layout class that child components
 * may also reference. In AHM, the production version defines this
 * in style.css instead. We keep it here for self-containment.
 * ---------------------------------------------------------------------------
 */
.minimal-container {
  width: 100%;
  box-sizing: border-box;
}
</style>
