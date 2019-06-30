package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Does layout of other components, either vertically or horizontally.
 */
@CompileStatic
@TypeChecked
class APSLayout extends APSContainerComponent<APSLayout> {
    static String ORIENTATION = "orientation"
    static String ORIENTATION_VERTICAL = "vertical"
    static String ORIENTATION_HORIZONTAL = "horizontal"
    static String BORDER_STYLE = "borderStyle"
    static String BORDER = "border"
    static String BORDER_TRUE = "true"
    static String BORDER_FALSE = "false"

    enum Orientation { vertical, horizontal }

    APSLayout() {

        this.componentProperties.type = "aps-layout"
        this.componentProperties.border = false
        this.componentProperties.borderStyle = "1px solid black"
    }

    APSLayout setOrientation( Orientation orientation ) {

        this.componentProperties.orientation = orientation.name(  )

        this
    }

    APSLayout setBorder(boolean border) {

        this.componentProperties.border = border

        this
    }

    APSLayout setBorderStyle(String borderStyle) {

        this.componentProperties.borderStyle = borderStyle

        this
    }
}
