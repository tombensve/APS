package se.natusoft.osgi.aps.groups.service;

import se.natusoft.osgi.aps.api.net.groups.service.NetTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Provides an implementation of NetTime.
 */
public class NetTimeProvider implements NetTime {

    //
    // Private Members
    //

    private se.natusoft.apsgroups.internal.protocol.NetTime.Time time;

    //
    // Constructors
    //

    /**
     * Creates a new NetTimeProvider.
     *
     * @param time An internal NetTime.Time instance.
     */
    public NetTimeProvider(se.natusoft.apsgroups.internal.protocol.NetTime.Time time) {
        this.time = time;
    }

    //
    // Methods
    //

    /**
     * Returns the number of milliseconds since Januray 1, 1970 in net time.
     */
    @Override
    public long getNetTime() {
        return this.time.getNetTimeValue();
    }

    /**
     * Returns the net time as a Date.
     */
    @Override
    public Date getNetTimeDate() {
        return this.time.getNetTimeDate();
    }

    /**
     * Returns the net time as a Calendar.
     */
    @Override
    public Calendar getNetTimeCalendar() {
        return this.time.getNetTimeCalendar();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    @Override
    public Calendar getNetTimeCalendar(Locale locale) {
        return this.time.getNetTimeCalendar(locale);
    }

    /**
     * Converts the net time to local time and returns as a Date.
     */
    @Override
    public Date getLocalTimeDate() {
        return this.time.getLocalTimeDate();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     */
    @Override
    public Calendar getLocalTimeCalendar() {
        return this.time.getLocalTimeCalendar();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    @Override
    public Calendar getLocalTimeCalendar(Locale locale) {
        return this.time.getLocalTimeCalendar(locale);
    }
}
