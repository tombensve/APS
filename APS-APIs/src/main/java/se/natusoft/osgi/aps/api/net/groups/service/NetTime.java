package se.natusoft.osgi.aps.api.net.groups.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This represents a common network time between members for handling date and time data.
 * The net time is synchronized between all members. Each receiver of net time diffs it with
 * local time and stores the diff so that they can convert to/from local/net time.
 */
public interface NetTime extends Serializable {

    /**
     * Returns the number of milliseconds since Januray 1, 1970 in net time.
     */
    public long getNetTime();

    /**
     * Returns the net time as a Date.
     */
    public Date getNetTimeDate();

    /**
     * Returns the net time as a Calendar.
     */
    public Calendar getNetTimeCalendar();

    /**
     * Returns the net time as a Calendar.
     *
     * @param locale The locale to use.
     */
    public Calendar getNetTimeCalendar(Locale locale);

    /**
     * Converts the net time to local time and returns as a Date.
     */
    public Date getLocalTimeDate();

    /**
     * Converts the net time to local time and returns as a Calendar.
     */
    public Calendar getLocalTimeCalendar();

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    public Calendar getLocalTimeCalendar(Locale locale);
}
