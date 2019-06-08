package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * A component having a numeric value, with a min and a max, and can be raised with key '>'
 * and lowered with key '<'.
 */
@CompileStatic
@TypeChecked
class APSNumber extends APSComponent {

    APSNumber() {
        this.componentProperties.type = "aps-number"
    }

    APSNumber setMin(double min) {
        this.componentProperties.min = min
        this
    }

    APSNumber setMax(double max) {
        this.componentProperties.max = max
        this
    }
}
