/**
 * This file manages all routing within the app.
 */
import { Routes, RouterModule } from '@angular/router';
// import {APSAdminWebComponent} from './apsadminweb/apsadminweb';

const routes: Routes = [
    // {path: '', redirectTo: '/main', pathMatch: 'full'},
    // {path: 'main',        component: APSAdminWebComponent},
];

export const routing = RouterModule.forRoot(routes);

