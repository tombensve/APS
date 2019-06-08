package se.natusoft.osgi.aps.web.models

class APSTextField extends APSComponent<APSTextField> {

    APSTextField() {
        this.componentProperties.type = "aps-text-field"
    }

    APSTextField setPlaceholder(String placeholder) {
        this.componentProperties.placehodler = placeholder
        this
    }

    APSTextField setLabel(String label) {
        this.componentProperties.label = label
        this
    }

    APSTextField setWidth(int width) {
        this.componentProperties.width = width
        this
    }
}
