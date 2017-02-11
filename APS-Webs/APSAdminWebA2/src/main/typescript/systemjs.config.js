System.config({
    transpiler: 'typescript',
    typescriptOptions: {emitDecoratorMetadata: true,
        target: "ES5",
        module: "commonjs"},
    map: {
        '@angular': 'node_modules/@angular',
        'rxjs': 'node_modules/rxjs',
        'vertx3-eventbus-client': 'node_modules/vertx3-eventbus-client',
        'vertx-eventbus': 'node_modules/vertx3-eventbus-client',
        // 'sockjs': 'static'
        'sockjs-client': 'node_modules/sockjs-client/dist',
        'sockjs': 'node_modules/sockjs-client/dist'
    },
    paths: {
        'node_modules/@angular/*': 'node_modules/@angular/*/bundles'
    },
    meta: {
        '@angular/*': {'format': 'cjs'}
    },
    packages: {
        'app': {main: 'main', defaultExtension: 'ts'},
        'rxjs': {main: 'Rx'},
        'vertx3-eventbus-client': {main: 'vertx-eventbus.js'},
        'vertx-eventbus': {main: 'vertx-eventbus.js'},
        // 'sockjs': {main: 'sockjs-0.3.4.min.js'},
        'sockjs-client': {main: 'sockjs.min.js'},
        'sockjs': {main: 'sockjs.min.js'},
        '@angular/core': {main: 'core.umd.min.js'},
        '@angular/common': {main: 'common.umd.min.js'},
        '@angular/compiler': {main: 'compiler.umd.min.js'},
        '@angular/router': {main: 'router.umd.min.js'},
        '@angular/platform-browser': {main: 'platform-browser.umd.min.js'},
        '@angular/platform-browser-dynamic': {main: 'platform-browser-dynamic.umd.min.js'}
    }
});
