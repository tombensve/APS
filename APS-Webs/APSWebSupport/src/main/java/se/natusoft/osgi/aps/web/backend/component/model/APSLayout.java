package se.natusoft.osgi.aps.web.backend.component.model;

public class APSLayout extends APSContainerComponent {

    public enum Orientation { vertical, horizontal }

    public APSLayout() {
        this.setProperty( "type", "aps-layout" );
        this.setProperty( "border", false );
        this.setProperty( "borderStyle", "1px solid black" );
    }

    public APSLayout setOrientation( Orientation orientation ) {
        this.setProperty("orientation", orientation.name());
        return this;
    }

    public APSLayout setBorder(boolean border) {
        this.setProperty( "border", border );
        return this;
    }

    public APSLayout setBorderStyle(String borderStyle) {
        this.setProperty( "borderStyle", borderStyle );
        return this;
    }
}
