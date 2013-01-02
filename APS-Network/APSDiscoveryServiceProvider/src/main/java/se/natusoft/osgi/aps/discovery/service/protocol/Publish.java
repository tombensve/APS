package se.natusoft.osgi.aps.discovery.service.protocol;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider;
import se.natusoft.osgi.aps.api.net.groups.service.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Publishes a service.
 */
public class Publish  implements Protocol {
    //
    // Private Members
    //

    /** The service description to read or write. */
    private ServiceDescription serviceDescription = null;

    private String fromMember = null;

    //
    // Constructors
    //

    /**
     * Creates a new Publish.
     *
     * @param serviceDescription The description of the service to publish.
     */
    public Publish(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /**
     * Creates a new Publish.
     */
    public Publish(Message message) {
        this.fromMember = message.getMemberId();
    }

    //
    // Methods
    //

    /**
     * @return The read service description.
     */
    public ServiceDescription getServiceDescription() {
        return this.serviceDescription;
    }

    /**
     * Reads from the stream into local data.
     *
     * @param dataStream The stream to read from.
     * @throws java.io.IOException
     */
    @Override
    public void read(DataInputStream dataStream) throws IOException {
        ServiceDescriptionProvider serviceDescriptionProvider = new ServiceDescriptionProvider();
        this.serviceDescription = serviceDescriptionProvider;
        serviceDescriptionProvider.setDescription(dataStream.readUTF());
        serviceDescriptionProvider.setServiceId(dataStream.readUTF());
        serviceDescriptionProvider.setVersion(dataStream.readUTF());
        serviceDescriptionProvider.setServiceHost(dataStream.readUTF());
        serviceDescriptionProvider.setServicePort(dataStream.readInt());
        serviceDescriptionProvider.setServiceURL(dataStream.readUTF());
    }

    /**
     * Writes from local data into specified stream.
     *
     * @param dataStream The stream to write to.
     * @throws java.io.IOException
     */
    @Override
    public void write(DataOutputStream dataStream) throws IOException {
        dataStream.writeInt(DiscoveryProtocol.PUBLISH);
        dataStream.writeUTF(serviceDescription.getDescription());
        dataStream.writeUTF(serviceDescription.getServiceId());
        dataStream.writeUTF(serviceDescription.getVersion());
        dataStream.writeUTF(serviceDescription.getServiceHost());
        dataStream.writeInt(serviceDescription.getServicePort());
        dataStream.writeUTF(serviceDescription.getServiceURL());
    }

    public String toString() {
        return "Publish: (From:" + this.fromMember + ") " + this.serviceDescription.getServiceId() + ":" + this.serviceDescription.getVersion();
    }
}
