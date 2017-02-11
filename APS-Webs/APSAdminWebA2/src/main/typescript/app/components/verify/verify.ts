import {Component, ViewEncapsulation} from '@angular/core';
import {FetchAdminWebs} from "../../services/FetchAdminWebs";
import {AdminAppModel} from "../../services/models";
import {EventBusProvider} from "../../services/EventBusProvider";
import {Vertx3EventBusFacade} from "../../services/Vertx3EventBusFacade";

//import {EventBusProvider} from "vertx-eventbus";

// import EventBus from 'vertx3-eventbus-client';
// let eventBus = EventBus("http://locahost:8080/eventbus", {});

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
    private eventBusProvider : Vertx3EventBusFacade;

    public constructor(adminWebs : FetchAdminWebs) {
        this.eventBusProvider = new Vertx3EventBusFacade("http://localhost:8080/eventbus", {});
        // this.eventBusProvider.send("myaddr", "{'content': 'Hello!'}", { });
        this.fetchAdminWebs = adminWebs;
        let admins : Array<AdminAppModel>;
        admins = this.fetchAdminWebs.getAdminApps();
        this.content = "";

        for ( let model of admins) {
            this.content += "name: " + model.name + "--> url: " + model.url + "\n";
        }
    }
}
