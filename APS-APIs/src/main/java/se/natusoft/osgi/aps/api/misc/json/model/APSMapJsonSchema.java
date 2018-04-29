package se.natusoft.osgi.aps.api.misc.json.model;

import java.util.Map;

/**
 * This represents a JSONish structure with `List<Object>`, `Map<String,Object>`, `String`, `Boolean`, `Number`, and null values,
 * where Òbject` __always__ refers to the same list of types.
 *
 * This represents a JSON schema according to APSGroovyToolsLib/MapJsonDocValidator. Needs GroovyRuntime deployed in server,
 * but can be used from Java.
 *
 * ----------------------------------------------------------------------------------------------------
 *
 * The reason for APSConfig and APSMapJsonSchema is that both really use exactly the same structure,
 * but contains very different information. I thought it might be confusing to declare them both as
 * maps. I think that the code is also more readable with this.
 *
 * To make things easier there is a util called APSConfigDelegator that takes a pure _Map_ and delegates
 * all calls to it. The _Map_ is also wrapped to be unmodifiable. APSConfigDelegator implements both
 * APSConfig and APSMapJsonSchema.
 */
public interface APSMapJsonSchema extends Map<String, Object> {

    static APSMapJsonSchema delegateTo(Map<String, Object> mapJson) {
        return new APSMapJsonDelegator(mapJson);
    }
}
