package se.natusoft.osgi.aps.api.core.meta;

/**
 * Implementations of this is returned by the APSMetaDataService.
 *
 * This API only contains the basic requirements. Any implementation should add Java Bean properties
 * for meta data it want to show.
 */
public interface APSMetaDataBean {

    /**
     * Identifies the owner of the meta data.
     */
    public String getOwnerName();

    /**
     * Identifies a version of the owner of the meta data.
     */
    public String getVersion();
}
