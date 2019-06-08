package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This is a container component with child components. It can have a heading and a footer.
 */
@CompileStatic
@TypeChecked
class APSPanel extends APSContainerComponent<APSPanel> {

    APSPanel() {
        this.componentProperties.type = "aps-panel"
    }

    APSPanel setHeading(String heading) {
        this.componentProperties.heading = heading
        this
    }

    APSPanel setFooter(String footer) {
        this.componentProperties.footer = footer
        this
    }

    /**
     * Sets the bootstrap style for the component.
     *
     * @param bsStyle The style to set.
     */
    APSPanel setBsStyle(String bsStyle) {
        this.componentProperties.bsStyle = bsStyle
        this
    }
}
