package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * A checkbox component.
 */
@CompileStatic
@TypeChecked
class APSCheckbox extends APSComponent {

    APSCheckbox() {
        this.componentProperties.type = "aps-check-box"
    }

    APSCheckbox setChecked(boolean checked) {
        this.componentProperties.value = checked ? "checked" : ""
        this
    }
}
