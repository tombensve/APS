package se.natusoft.aps.platform;

import se.natusoft.docutations.Note;

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
public class BundleException extends Exception {
    static final long serialVersionUID = 3571095144220455665L;

    private final int type;

    public static final int UNSPECIFIED = 0;
    public static final int UNSUPPORTED_OPERATION = 1;
    public static final int INVALID_OPERATION = 2;
    public static final int MANIFEST_ERROR = 3;
    public static final int RESOLVE_ERROR = 4;
    public static final int ACTIVATOR_ERROR = 5;
    public static final int SECURITY_ERROR = 6;
    public static final int STATECHANGE_ERROR = 7;

    public static final int NATIVECODE_ERROR = 8;

    public static final int DUPLICATE_BUNDLE_ERROR = 9;

    public static final int START_TRANSIENT_ERROR = 10;

    public BundleException( String msg, Throwable cause ) {
        this( msg, UNSPECIFIED, cause );
    }

    public BundleException( String msg ) {
        this( msg, UNSPECIFIED );
    }

    public BundleException( String msg, int type, Throwable cause ) {
        super( msg, cause );
        this.type = type;
    }

    public BundleException( String msg, int type ) {
        super( msg );
        this.type = type;
    }

    public Throwable getNestedException() {
        return getCause();
    }

    public Throwable getCause() {
        return super.getCause();
    }

    public Throwable initCause( Throwable cause ) {
        return super.initCause( cause );
    }

    public int getType() {
        return type;
    }
}

