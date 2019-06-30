package se.natusoft.osgi.aps.web.models

class APSRadioSet extends APSComponent<APSRadioSet> {

    private List radios = []

    APSRadioSet() {
        this.componentProperties.type = "aps-radio-set"
        this.componentProperties.radios = radios
    }

    APSRadioSet addRadio(String id, String label) {
        Map<String, Object> radio = [:]
        radio.id = id
        radio.label = label
        this.radios << radio

        this
    }

    APSRadioSet leftShift(Map<String, String> radio) {
        this.radios << radio

        this
    }

    APSRadioSet leftShift(String id, String label) {
        Map<String, Object> radio = [:]
        radio.id = id
        radio.label = label
        this.radios << radio

        this
    }
}
