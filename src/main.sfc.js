// AHM Hello World — SFC-Loader entry point.
// Loads Vue + Router via sfcBootstrap.js, mounts to #app.
// style.css is loaded via <link> in index.html (compat with older WebViews
// that don't support the CSS Module Scripts `with { type: 'css' }` syntax).

import { bootstrapSfcApp } from './util/sfcBootstrap.js';

(window.__ahmLog || function(){})('main.sfc: entered');

bootstrapSfcApp().then(({ createApp, Main, router }) => {
  (window.__ahmLog || function(){})('main.sfc: bootstrap resolved');
  const app = createApp(Main);
  if (router) app.use(router);
  app.mount('#app');
  (window.__ahmLog || function(){})('main.sfc: mounted');
  const st = document.getElementById('boot-status');
  if (st) st.remove();
}).catch(err => {
  const eb = document.getElementById('err-banner');
  if (eb) { eb.style.display='block'; eb.textContent = 'Bootstrap failed:\n' + (err.stack || err.message || err); }
  console.error('App initialization failed:', err);
});
