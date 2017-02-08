import {Component, ViewEncapsulation} from '@angular/core';
import {FetchAdminWebs} from "../../services/FetchAdminWebs";
import {AdminAppModel} from "../../services/models";
//import {EventBus} from "vertx-eventbus";

import EventBus from 'vertx3-eventbus-client';
export let eventBus = EventBus("http://locahost:8080/eventbus", {});

/**
 * This is the main application top level component.
 */
@Component({
    selector: 'admin-verify',
    templateUrl: 'app/components/verify/verify-tpl.html',
    encapsulation:ViewEncapsulation.None
})
export class VerifyComponent {
    public content : string;
    private fetchAdminWebs : FetchAdminWebs;
    //private eventBus : EventBus;

    public constructor(adminWebs : FetchAdminWebs) {
        eventBus.send({});
        this.fetchAdminWebs = adminWebs;
        let admins : Array<AdminAppModel>;
        admins = this.fetchAdminWebs.getAdminApps();
        this.content = "";

        for ( let model of admins) {
            this.content += "name: " + model.name + "--> url: " + model.url + "\n";
        }
    }
}
