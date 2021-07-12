package se.natusoft.aps.platform;


import se.natusoft.docutations.Note;

import java.util.EventObject;

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
public class BundleEvent extends EventObject {

    static final long serialVersionUID = 4080640865971756012L;

    private final Bundle bundle;

    private final int type;

    public final static int INSTALLED = 0x00000001;

    public final static int STARTED = 0x00000002;

    public final static int STOPPED = 0x00000004;

    public final static int UPDATED = 0x00000008;

    public final static int UNINSTALLED = 0x00000010;

    public final static int RESOLVED = 0x00000020;

    public final static int UNRESOLVED = 0x00000040;

    public final static int STARTING = 0x00000080;

    public final static int STOPPING = 0x00000100;

    public final static int LAZY_ACTIVATION = 0x00000200;

    public BundleEvent( int type, Bundle bundle ) {
        super( bundle );
        this.bundle = bundle;
        this.type = type;
    }

    public Bundle getBundle() {
        return bundle;
    }


    public int getType() {
        return type;
    }
}
