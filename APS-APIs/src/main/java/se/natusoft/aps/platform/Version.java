package se.natusoft.aps.platform;

import se.natusoft.docutations.Note;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

@Note(
        {
                "APS used to be OSGi services deployed in an OSGi container. That is no longer the case!",
                "APS services will not work if deployed in an OSGi container, some might, but in general",
                "not! APS is still using some OSGi APIs, but APS provides implementation of those. ",
                "Thereby all dependencies on OSGi is removed and those few OSGi APIs used are copied from",
                "OSGi. They are however renamed with an APS prefix and moved to aps package, to lessen ",
                "confusion. In time the contents also might change from the OSGi original. Doing this API",
                "rename and package move allows for that. This should no longer be seen as OSGi, it hasn't",
                "been for some time."
        }
)
public class Version implements Comparable {

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;
    private static final String	SEPARATOR= ".";

    public static final Version emptyVersion = new Version(0, 0, 0);

    public Version(int major, int minor, int micro) {
        this(major, minor, micro, null);
    }

    public Version(int major, int minor, int micro, String qualifier) {
        if (qualifier == null) {
            qualifier = ""; //$NON-NLS-1$
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
        validate();
    }

    public Version(String version) {
        int maj = 0;
        int min = 0;
        int mic = 0;
        String qual = ""; //$NON-NLS-1$

        try {
            StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
            maj = Integer.parseInt(st.nextToken());

            if (st.hasMoreTokens()) {
                st.nextToken(); // consume delimiter
                min = Integer.parseInt(st.nextToken());

                if (st.hasMoreTokens()) {
                    st.nextToken(); // consume delimiter
                    mic = Integer.parseInt(st.nextToken());

                    if (st.hasMoreTokens()) {
                        st.nextToken(); // consume delimiter
                        qual = st.nextToken();

                        if (st.hasMoreTokens()) {
                            throw new IllegalArgumentException("invalid format"); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        catch ( NoSuchElementException e) {
            throw new IllegalArgumentException("invalid format"); //$NON-NLS-1$
        }

        major = maj;
        minor = min;
        micro = mic;
        qualifier = qual;
        validate();
    }

    private void validate() {
        if (major < 0) {
            throw new IllegalArgumentException("negative major"); //$NON-NLS-1$
        }
        if (minor < 0) {
            throw new IllegalArgumentException("negative minor"); //$NON-NLS-1$
        }
        if (micro < 0) {
            throw new IllegalArgumentException("negative micro"); //$NON-NLS-1$
        }
        char[] chars = qualifier.toCharArray();
        for (int i = 0, length = chars.length; i < length; i++) {
            char ch = chars[i];
            if (('A' <= ch) && (ch <= 'Z')) {
                continue;
            }
            if (('a' <= ch) && (ch <= 'z')) {
                continue;
            }
            if (('0' <= ch) && (ch <= '9')) {
                continue;
            }
            if ((ch == '_') || (ch == '-')) {
                continue;
            }
            throw new IllegalArgumentException(
                    "invalid qualifier: " + qualifier); //$NON-NLS-1$
        }
    }

    public static Version parseVersion( String version) {
        if (version == null) {
            return emptyVersion;
        }

        version = version.trim();
        if (version.length() == 0) {
            return emptyVersion;
        }

        return new Version(version);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMicro() {
        return micro;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String toString() {
        int q = qualifier.length();
        StringBuffer result = new StringBuffer(20 + q);
        result.append(major);
        result.append(SEPARATOR);
        result.append(minor);
        result.append(SEPARATOR);
        result.append(micro);
        if (q > 0) {
            result.append(SEPARATOR);
            result.append(qualifier);
        }
        return result.toString();
    }

    public int hashCode() {
        return (major << 24) + (minor << 16) + (micro << 8)
                + qualifier.hashCode();
    }

    public boolean equals(Object object) {
        if (object == this) { // quicktest
            return true;
        }

        if (!(object instanceof org.osgi.framework.Version )) {
            return false;
        }

        Version other = ( Version ) object;
        return (major == other.major) && (minor == other.minor)
                && (micro == other.micro) && qualifier.equals(other.qualifier);
    }

    public int compareTo(Object object) {
        if (object == this) { // quicktest
            return 0;
        }

        Version other = ( Version ) object;

        int result = major - other.major;
        if (result != 0) {
            return result;
        }

        result = minor - other.minor;
        if (result != 0) {
            return result;
        }

        result = micro - other.micro;
        if (result != 0) {
            return result;
        }

        return qualifier.compareTo(other.qualifier);
    }
}
