package se.natusoft.osgi.aps.web.models

class APSTextArea extends APSComponent<APSTextArea> {

    APSTextArea() {
        this.componentProperties.type = "aps-text-area"
    }

    APSTextArea setRows(int rows) {
        this.componentProperties.rows = rows
        this
    }

    APSTextArea setCols(int cols) {
        this.componentProperties.cols = cols
        this
    }

    APSTextArea size(int cols, int rows) {
        this.componentProperties.cols = cols
        this.componentProperties.rows = rows
        this
    }

}
