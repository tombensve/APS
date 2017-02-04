import { NgModule }                             from '@angular/core';
import { BrowserModule }                        from '@angular/platform-browser';
import { Routes, RouterModule }                         from '@angular/router';
// import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import {LocationStrategy, HashLocationStrategy} from '@angular/common';
import {APSAdminWebComponent}                   from './components/apsadminweb/apsadminweb';
import Verify                                   from './components/verify/verify';
import { FetchAdminWebs }                       from './services/FetchAdminWebs';
import { routing }                              from './components/app.routing';

@NgModule({
    imports: [
        routing,
        BrowserModule,
        RouterModule.forRoot([
            { path: '',                    component: APSAdminWebComponent }
        ])
    ],
    declarations: [
        APSAdminWebComponent, Verify
    ],
    providers:    [FetchAdminWebs, Verify,
        {provide: LocationStrategy, useClass: HashLocationStrategy}],
    bootstrap:    [ APSAdminWebComponent ]
})
export class AppModule { }
