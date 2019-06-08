package se.natusoft.osgi.aps.web.models

class APSSelect extends APSComponent {

    private List options = []

    APSSelect() {
        this.componentProperties.type = "aps-select"
        this.componentProperties.options = options
    }

    APSSelect addOption( String value, String label) {
        Map<String, Object> option = [:]
        option.value = value
        option.label = label
        this.optoins << option

        this
    }

    APSSelect leftShift( Map<String, String> options) {
        this.options << options

        this
    }

    APSSelect leftShift( String value, String label) {
        Map<String, Object> option = [:]
        option.value = value
        option.label = label
        this.options << option

        this
    }
}
