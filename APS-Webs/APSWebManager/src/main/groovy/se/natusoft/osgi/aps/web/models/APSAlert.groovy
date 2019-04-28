package se.natusoft.osgi.aps.web.models;

class APSAlert extends APSComponent {

    APSAlert() {
        this.setProperty( "type", "aps-alert" )
    }

    APSAlert setAlertType(String alertType) {
        this.setProperty( "bsType", alertType )
        return this
    }
}
