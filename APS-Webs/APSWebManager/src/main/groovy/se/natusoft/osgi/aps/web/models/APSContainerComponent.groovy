package se.natusoft.osgi.aps.web.models
/**
 * Models components that are of container type having child components.
 */
class APSContainerComponent extends APSComponent {

    private List<APSComponent> children = []

    APSContainerComponent() {

        this.componentProperties.children = children
    }

    void addChild(APSComponent component) {
        this.children << component
    }

}
