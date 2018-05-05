package se.natusoft.osgi.aps.api.misc.json.model;

import java.util.Map;

/**
 * This represents a JSONish structure with `List<Object>`, `Map<String,Object>`, `String`, `Boolean`, `Number`, and null values,
 * where Object` __always__ refers to the same list of types.
 *
 * This represents a JSON schema according to APSGroovyToolsLib/MapJsonDocValidator. Needs GroovyRuntime deployed in server,
 * but can be used from Java.
 *
 * Note that this interface can be subclassed for specific MapJSon objects.
 *
 * ----------------------------------------------------------------------------------------------------
 *
 * The reason for APSMapJson and APSMapJsonSchema is that both really use exactly the same structure,
 * but contains very different information. I thought it might be confusing to declare them both as
 * maps. I think that the code is also more readable with this.
 *
 * To make things easier there is a util called APSMapJsonDelegator that takes a pure _Map_ and delegates
 * all calls to it. The _Map_ is also wrapped to be unmodifiable. APSMapJsonDelegator implements both
 * APSMapJson and APSMapJsonSchema.
 */
public interface APSMapJson extends Map<String, Object> {

    static APSMapJson delegateTo(Map<String, Object> mapJson) {
        return new APSMapJsonDelegator(mapJson);
    }
}
