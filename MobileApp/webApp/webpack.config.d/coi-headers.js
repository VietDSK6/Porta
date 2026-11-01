// Cross-origin isolation for the dev server.
//
// The local Room database on web is backed by SQLite-Wasm's OPFS VFS
// (`sqlite3.oo1.OpfsDb` in shared/sqlite-wasm-worker/worker.js). That VFS only works when the
// page is *cross-origin isolated* (`crossOriginIsolated === true`), which requires these two
// response headers. Without them the worker can't open the database and all local data
// silently comes back empty.
//
// COEP is `credentialless` (not `require-corp`) so linked official sources can still open normally.
//
// NOTE: this only covers the dev server. In production the web host must send the same two
// headers for OPFS to work.
config.devServer = config.devServer || {};
config.devServer.headers = Object.assign({}, config.devServer.headers, {
    "Cross-Origin-Opener-Policy": "same-origin",
    "Cross-Origin-Embedder-Policy": "credentialless",
});
