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
public class ServiceEvent {

    static final long serialVersionUID = 8792901483909409299L;

    private final ServiceReference reference;

    private final int type;

    public final static int	REGISTERED = 0x00000001;

    public final static int	MODIFIED = 0x00000002;

    public final static int	UNREGISTERING = 0x00000004;

    public final static int	MODIFIED_ENDMATCH = 0x00000008;

    public ServiceEvent( int type, ServiceReference reference) {
        //super(reference);
        this.reference = reference;
        this.type = type;
    }
    public ServiceReference getServiceReference() {
        return reference;
    }

    public int getType() {
        return type;
    }

}
