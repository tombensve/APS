package se.natusoft.osgi.aps.metadata;

import se.natusoft.osgi.aps.api.core.meta.APSMetaDataService;
import se.natusoft.osgi.aps.api.core.meta.MetaData;
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This provides an implementation of the APSMetaDataService.
 */
@SuppressWarnings("unused")
@OSGiServiceProvider
public class APSMetaDataServiceProvider implements APSMetaDataService {

    //
    // Private Members
    //

    private Map<String, MetaData> metaDatas = Collections.synchronizedMap(new HashMap<>());

    //
    // Methods
    //

    /**
     * Returns the meta data for the specified owner. This will return an empty MetaData object if no setMetaData(...) have been
     * called and this is the first call to getMetaData(...). Using only this method makes the service provide the MetaData instance.
     *
     * @param owner The owner to get meta data for. Make sure this is very unique!
     */
    @Override
    public MetaData getMetaData(String owner) {
        MetaData metaData = this.metaDatas.get(owner);
        if (metaData == null) {
            metaData = new MetaDataProvider();
            this.metaDatas.put(owner, metaData);
        }
        return metaData;
    }

    /**
     * Sets a meta data object. In this case it is upp to the caller to make sure the set MetaData object is concurrently callable.
     * This is for when you don't want to use the default implementation of MetaData, but wan't to provide your own. One reason for
     * this could be to provide live realtime data.
     * <p>
     * After this method have been called, a call to getMetaData(...) will return the set object for the owner.
     *
     * @param owner    The owner to set new MetaData object for.
     * @param metaData The meta data object to set.
     */
    @Override
    public void setMetaData(String owner, MetaData metaData) {
        this.metaDatas.put(owner, metaData);
    }

}
