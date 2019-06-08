package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * Date selector component.
 */
@CompileStatic
@TypeChecked
class APSDate extends APSComponent {

    APSDate() {
        this.componentProperties.type = "aps-date"
    }

    APSDate setStartValue(String yymmdd) {
        this.componentProperties.startValue = yymmdd
        this
    }

    APSDate setStartValue(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        this.componentProperties.startValue = sdf.format( date )
        this
    }

    APSDate setStartValue(TemporalAccessor ta) {
        this.componentProperties.startValue = DateTimeFormatter.ISO_LOCAL_DATE.format( ta )
        this
    }

    APSDate setDisabled(boolean disabled) {
        this.componentProperties.disabled = "" + disabled
        this
    }
}
