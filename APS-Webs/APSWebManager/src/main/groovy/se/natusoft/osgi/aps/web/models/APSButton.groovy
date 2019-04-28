package se.natusoft.osgi.aps.web.models;

class APSButton extends APSComponent<APSButton> {

    @SuppressWarnings( "WeakerAccess" )
    APSButton() {
        this.setProperty( "type", "aps-button" )
    }

    APSButton(String componentAddress) {
        this()
    }

    APSButton setLabel(String label) {
        this.setProperty( "label", label )
        return this
    }

    APSButton setDisabled(boolean disabled) {
        this.setProperty( "disabled", disabled )
        return this
    }

    APSButton setStyle(String bootstrapStyle) {
        this.setProperty( "style", bootstrapStyle )
        return this
    }

    APSButton setClass(String clazz) {
        this.setProperty( "class", clazz )
        return this
    }
}
