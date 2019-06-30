package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Models components that are of container type having child components.
 */
@CompileStatic
@TypeChecked
class APSContainerComponent<Component> extends APSComponent<Component> {

    private List children = [ ]

    APSContainerComponent() {

        this.componentProperties["children"] = children
    }

    Component addChild( APSComponent component ) {
        this.children << component.componentProperties
        return this as Component
    }

    Component leftShift( APSComponent component ) {
        addChild( component )
    }

}
