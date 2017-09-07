import {Component, ViewEncapsulation, forwardRef, Inject} from '@angular/core';
import {AdminAppsService} from "../../services/AdminAppsService";
//import {AdminAppModel} from "../../services/models";
import {EventBusService} from "../../services/EventBusService";
import {EventBus} from "../../services/EventBus";

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

    private eventBusProvider : EventBus;

    public constructor(@Inject(forwardRef(() => AdminAppsService)) public adminAppsService : AdminAppsService) {

        this.eventBusProvider = new EventBusService();
        this.eventBusProvider.connect("http://192.168.1.60:9080/eventbus/");

        this.eventBusProvider.registerHandler("aps.adminweb", function handler(event:object) {

        } , {});

        let headers : Array<string>;
        headers = [];
        this.eventBusProvider.publish("aps.adminweb", "{\"content\": \"Hello!\"}", headers);

        // let admins : Array<AdminAppModel>;
        // admins = this.adminAppsService.getAdminApps();
        // this.content = "";
        //
        // for ( let model of admins) {
        //     this.content += "name: " + model.name + "--> url: " + model.url + "\n";
        // }
    }
}
