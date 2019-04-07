package se.natusoft.osgi.aps.web.backend.component.model;

public class APSButton extends APSComponent<APSButton> {

    public APSButton() {
        this.setProperty( "type", "aps-button" );
    }

    public APSButton setLabel(String label) {
        this.setProperty( "label", label );
        return this;
    }

    public APSButton setDisabled(boolean disabled) {
        this.setProperty( "disabled", disabled );
        return this;
    }

    public APSButton setStyle(String bootstrapStyle) {
        this.setProperty( "style", bootstrapStyle );
        return this;
    }

    public APSButton setClass(String clazz) {
        this.setProperty( "class", clazz );
        return this;
    }
}
