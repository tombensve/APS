package se.natusoft.osgi.aps.api.core;

import java.io.Serializable;

/**
 * Classes implementing this interface are not Serializable in themselves, but have certain data
 * which can be serialized and be provided from deserialized data. In some cases the whole class
 * might be serializable, but there is not point in serializing everything.
 */
public interface APSSerializableData {

    /**
     * @return A Serializable object of the type provided by getSerializedType().
     */
    Serializable toSerializable();

    /**
     * Receives a deserialized object of the type provided by getSerializedType().
     *
     * @param serializable The deserialized object received.
     */
    void fromDeserialized(Serializable serializable);

    /**
     * @return The serialized type.
     */
    Class getSerializedType();
}
