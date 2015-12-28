package se.natusoft.osgi.aps.discoveryservice

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider

/**
 * General static tools and constants.
 */
class ReadWriteTools {

    static final byte HB_ADD = 1
    static final byte HB_REMOVE = 2

    static final short PROTOCOL_VERSION = 1;

    /**
     * Converts a ServiceDescription to a byte array.
     *
     * @param serviceDescription The ServiceDescription to convert.
     */
    static byte[] toBytes(byte headerByte, ServiceDescription serviceDescription) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream()
        ObjectOutputStream ooStream = new ObjectOutputStream(byteStream);
        writeServiceDescription(headerByte, serviceDescription, ooStream)
        ooStream.flush()
        ooStream.close()

        return byteStream.toByteArray()
    }

    /**
     * Reusable writer for writing a ServiceDescription plus header byte.
     *
     * @param headerByte The header byte to write.
     * @param serviceDescription The service description to write.
     * @param ooStream The stream to write to.
     * @throws IOException
     */
    static void writeServiceDescription(byte headerByte, ServiceDescription serviceDescription, ObjectOutputStream ooStream)
            throws IOException {
        ooStream.writeShort(PROTOCOL_VERSION)
        ooStream.writeByte(headerByte)
        ooStream.writeUTF(serviceDescription.description)
        ooStream.writeUTF(serviceDescription.serviceHost)
        ooStream.writeUTF(serviceDescription.serviceId)
        ooStream.writeInt(serviceDescription.servicePort)
        ooStream.writeObject(serviceDescription.serviceProtocol)
        ooStream.writeUTF(serviceDescription.serviceURL)
        ooStream.writeUTF(serviceDescription.version)
    }

    /**
     * Converts a byte array into a ServiceDescription.
     *
     * @param serviceDescription The service description to populate.
     * @param bytes The bytes to convert.
     *
     * @throws IOException on failure (i.e the bytes does not contain a ServiceDescription!).
     */
    static byte fromBytes(ServiceDescriptionProvider serviceDescription, byte[] bytes) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)
        ObjectInputStream ooStream = new ObjectInputStream(byteStream)
        byte headerByte = readServiceDescription(serviceDescription, ooStream)
        ooStream.close()

        return headerByte
    }

    /**
     * Reusable object reader of a ServiceDescription plus header byte.
     *
     * @param serviceDescription The ServiceDescription instance to read into.
     * @param ooStream The stream to read from.
     *
     * @return The header byte.
     *
     * @throws IOException
     */
    static byte readServiceDescription(ServiceDescriptionProvider serviceDescription, ObjectInputStream ooStream)
            throws IOException {
        short version = ooStream.readShort();
        if (version == PROTOCOL_VERSION) {
            byte headerByte = ooStream.readByte()
            serviceDescription.description = ooStream.readUTF()
            serviceDescription.serviceHost = ooStream.readUTF()
            serviceDescription.serviceId = ooStream.readUTF()
            serviceDescription.servicePort = ooStream.readInt()
            serviceDescription.serviceProtocol = ooStream.readObject() as ServiceDescription.Protocol
            serviceDescription.serviceURL = ooStream.readUTF()
            serviceDescription.version = ooStream.readUTF()

            return headerByte
        }
        throw new IOException("Unsupported version (${version}) received!");
    }
}
