package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Models components that are of container type having child components.
 */
@CompileStatic
@TypeChecked
class APSContainerComponent extends APSComponent {

    private List<APSComponent> children = []

    APSContainerComponent() {

        this.componentProperties.children = children
    }

    void addChild(APSComponent component) {
        this.children << component
    }

}
