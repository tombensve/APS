package se.natusoft.osgi.aps.web.models;

 class APSLayout extends APSContainerComponent {

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
