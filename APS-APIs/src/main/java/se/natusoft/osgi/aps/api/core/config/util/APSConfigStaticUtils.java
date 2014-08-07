package se.natusoft.osgi.aps.api.core.config.util;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

/**
 * This class contains static utility methods and is not instantiable.
 */
public class APSConfigStaticUtils {

    private APSConfigStaticUtils() {}

    /**
     * Extracts an APSConfigEditModel from an APSConfigReference. If the reference tip model is an APSConfigEditModel
     * it is returned, if not its parent is returned.
     *
     * @param ref The ref to get the APSConfigEditModel from.
     */
    public static APSConfigEditModel refToEditModel(APSConfigReference ref) {
        APSConfigValueEditModel cvem = ref.getConfigValueEditModel();
        return (cvem instanceof APSConfigEditModel) ? (APSConfigEditModel)cvem : cvem.getParent();
    }

}
