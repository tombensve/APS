import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {RouterModule} from "@angular/router";
// import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import {LocationStrategy, HashLocationStrategy} from "@angular/common";
import {APSAdminWebComponent} from "./components/apsadminweb/apsadminweb";
import {VerifyComponent} from "./components/verify/verify";
import {AdminAppsService} from "./services/AdminAppsService";
import {routing} from "./components/app.routing";

@NgModule({
    imports: [
        routing,
        BrowserModule
    ],
    declarations: [
        APSAdminWebComponent, VerifyComponent
    ],
    providers: [
        AdminAppsService,
        APSAdminWebComponent,
        VerifyComponent,
        {provide: LocationStrategy, useClass: HashLocationStrategy}
    ],
    bootstrap: [APSAdminWebComponent]
})
export class AppModule {
}
