package se.natusoft.osgi.aps.api.core.config.model;

import se.natusoft.osgi.aps.api.misc.json.model.APSMapJson;
import se.natusoft.osgi.aps.api.util.APSMapJsonDelegator;

import java.util.Map;

/**
 * This represents a JSONish structure with `List<Object>`, `Map<String,Object>`, `String`, `Boolean`, `Number`, and null values,
 * where Òbject` __always__ refers to the same list of types.
 *
 * This is for containing configuration values.
 */
public interface APSConfig extends APSMapJson {

    class APSConfigDelegator extends APSMapJsonDelegator implements APSConfig {
        public APSConfigDelegator(Map<String, Object> content) {
            super(content);
        }
    }

    static APSConfig delegateTo(Map<String, Object> mapJson) {
        return new APSConfigDelegator(mapJson);
    }
}
