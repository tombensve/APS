package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * An alert component.
 *
 * This also listens to this bus message:
 *
 *     {
 *         aps: {
 *             type: "aps-alert"
 *         },
 *         content: {
 *             targetId: "<id of this component>",
 *             markdown: "alert text in markdown format"
 *         }
 *     }
 *
 * This component will be invisible until it receives a message.
 */
@CompileStatic
@TypeChecked
class APSAlert extends APSComponent {

    APSAlert() {
        this.componentProperties.type = "aps-alert"
    }

    /**
     * @param alertType "warning", "error", ...
     */
    APSAlert setAlertType(String alertType) {
        this.componentProperties.alertType = alertType
        return this
    }

    /**
     * If set, the alert will go away after this time.
     *
     * @param ms The time before hiding in milliseconds.
     */
    APSAlert setHideIn(int ms) {
        this.componentProperties.hideIn = ms
        this
    }
}
