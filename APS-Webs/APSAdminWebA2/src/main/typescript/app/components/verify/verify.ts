import {Component, ViewEncapsulation, forwardRef, Inject} from '@angular/core';
import {AdminAppsService} from "../../services/AdminAppsService";
import {AdminAppModel} from "../../services/models";
import {Vertx3EventBusFacade} from "../../services/Vertx3EventBusFacade";
//import {EventBusProvider} from "../../services/EventBusProvider";

/**
 * This is the main application top level component.
 */
@Component({
    selector: 'admin-verify',
    templateUrl: './app/components/verify/verify-tpl.html',
    encapsulation: ViewEncapsulation.None
})
export class VerifyComponent {
    public content : string;
    private eventBusProvider : Vertx3EventBusFacade;

    public constructor(@Inject(forwardRef(() => AdminAppsService)) public adminAppsService : AdminAppsService) {

        this.eventBusProvider = new Vertx3EventBusFacade("http://localhost:8080/eventbus", {});
        // this.eventBusProvider.send("myaddr", "{'content': 'Hello!'}", {});
        let admins : Array<AdminAppModel>;
        admins = this.adminAppsService.getAdminApps();
        this.content = "";

        for ( let model of admins) {
            this.content += "name: " + model.name + "--> url: " + model.url + "\n";
        }
    }
}
