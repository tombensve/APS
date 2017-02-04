import {Component, ViewEncapsulation} from '@angular/core';
import {FetchAdminWebs} from "../../services/FetchAdminWebs";
import {AdminAppModel} from "../../services/models";

/**
 * This is the main application top level component.
 */
@Component({
    selector: 'admin-verify',
    templateUrl: 'app/components/verify/verify-tpl.html',
    encapsulation:ViewEncapsulation.None
})
export default class VerifyComponent {
    public content : string;
    private fetchAdminWebs : FetchAdminWebs;

    public constructor(adminWebs : FetchAdminWebs) {
        this.fetchAdminWebs = adminWebs;
        let admins : Array<AdminAppModel>;
        admins = this.fetchAdminWebs.getAdminApps();
        this.content = "";

        for ( let model of admins) {
            this.content += "name: " + model.name + "--> url: " + model.url + "\n";
        }
    }
}
