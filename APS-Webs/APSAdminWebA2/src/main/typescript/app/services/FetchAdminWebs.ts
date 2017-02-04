import { Injectable } from '@angular/core';
import { AdminAppModel } from './models'

/**
 *
 */
@Injectable()
export class FetchAdminWebs {

    private adminApps: Array<AdminAppModel> = [];

    public constructor() {
        this.adminApps.push( new AdminAppModel( "test" , "http://www.natusoft.se/" ) );
    }

    public getAdminApps() : Array<AdminAppModel> {
        return this.adminApps;
    }
}
