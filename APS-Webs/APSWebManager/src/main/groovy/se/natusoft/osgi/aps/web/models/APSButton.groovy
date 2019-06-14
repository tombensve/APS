package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * A button component.
 */
@CompileStatic
@TypeChecked
class APSButton extends APSComponent<APSButton> {

    @SuppressWarnings( "WeakerAccess" )
    APSButton() {
        this.componentProperties.type = "aps-button"
    }

    APSButton setLabel( String label ) {
        this.componentProperties.label = label
        this
    }

    APSButton setDisabled( boolean disabled ) {
        this.componentProperties.disabled = disabled
        this
    }

    APSButton setStyle( String bootstrapStyle ) {
        this.componentProperties.style = bootstrapStyle
        this
    }

    APSButton setStyleClass( String clazz ) {
        this.componentProperties[ "class" ] = clazz
        this
    }
}
