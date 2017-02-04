/**
 * This file manages all routing within the app.
 */
import { Routes, RouterModule } from '@angular/router';
import {APSAdminWebComponent} from './apsadminweb/apsadminweb';

const routes: Routes = [
    {path: '',        component: APSAdminWebComponent},
    // {path: 'product', component: ProductDetailComponent}
];

export const routing = RouterModule.forRoot(routes);

