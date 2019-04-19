package se.natusoft.osgi.aps.web.backend.component.model;

public class APSAlert extends APSComponent {

    public APSAlert() {
        this.setProperty( "type", "aps-.alert" );
    }

    public APSAlert setAlertType(String alertType) {
        this.setProperty( "bsType", alertType );
        return this;
    }
}
