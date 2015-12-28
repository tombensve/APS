package se.natusoft.osgi.aps.tcpipsvc.meta

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.meta.APSMetaDataBean
import se.natusoft.osgi.aps.api.core.meta.APSMetaDataService
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 *
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class APSTCPIPMetaDataServiceProvider implements APSMetaDataService {

    @Managed(name = "META-DATA")
    private APSTCPIPServiceMetaData metaData

    /**
     * Returns meta data about the service as a JSON object..
     */
    @Override
    APSMetaDataBean getMetaDataBean() {
        return this.metaData
    }

}
