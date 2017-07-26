/**
 * This is the main entry point to the APSAdminWeb application.
 */
import 'zone.js';
import 'reflect-metadata';
import '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule }  from './app.module';
import 'rxjs'

// declare function require(path: string) : any;
// var helper = require('node_modules/');

// This does not work. Will be copied to main.js, but has no effect.
// var coreJSDep = require("../node_modules/core-js/client/shim.min.js");
// var zoneJSDep = require("../node_modules/zone.js/dist/zone.js");
// var sockJSDep = require("../node_modules/sockjs-client/dist/sockjs.min.js");
// var vertxEventBusJSDep = require("../node_modules/vertx3-eventbus-client/vertx-eventbus.js");

// From http://stackoverflow.com/questions/22842389/how-to-import-a-js-library-without-definition-file-in-typescript-file:
// Does not work either. Is not transpiled to main.js.
// declare module "../node_modules/core-js/client/shim.min.js" {
//    var coreJSDep : any;
//    export = coreJSDep;
// }
// declare module "../node_modules/zone.js/dist/zone.js" {
//     var zoneJSDep : any;
//     export = zoneJSDep;
// }
// declare module "../node_modules/sockjs-client/dist/sockjs.min.js" {
//     var sockJSDep : any;
//     export = sockJSDep;
// }
// declare module "../node_modules/vertx3-eventbus-client/vertx-eventbus.js" {
//     var vertxEventBusJSDep : any;
//     export = vertxEventBusJSDep;
// }

// https://github.com/Microsoft/TypeScript/issues/3019:
// Noop, this doesn't work either, not transpiled.
// import coreJS from '../node_modules/core-js/client/shim.min.js';

platformBrowserDynamic().bootstrapModule(AppModule);
