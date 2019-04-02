package se.natusoft.osgi.aps.web;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a builder that builds a APSWebManager GUI by producing JSON as a Map.
 *
 * ### Example
 *
 *     new APSWebManagerGUIBuilder()
 *         .comp("aps-layout").p("id", "page").p("name", "page").p("orientation", "vertical")
 *         .children(
 *            new APSWebManagerGUIBuilder()
 *            .comp("aps-alert").p("id", "aps-default-alert").p("name", "alert-comp").p("bsType", "danger")
 *            .headers(new MapBilder().entry("routing", new MapBuilder().entry("outgoing", "client")
 *            .entry("incoming", "client").result()))
 *            .comp("aps-layout") ...
 *         ).resultAsMap();
 */
public class APSWebManagerGUIBuilder {

    private Object jsonTop = null;
    private Map<String, Object> json = null;
    private List<Map<String, Object>> jsonList = null;

    /**
     * Starts a new component.
     *
     * @param name The name of the component.
     *
     * @return The builder instance.
     */
    APSWebManagerGUIBuilder comp(String name) {

        if (this.json == null) {
            this.json = new LinkedHashMap<>(  );
            this.jsonTop = this.json;
        }
        else if (this.jsonList == null){
            this.jsonList = new LinkedList<>(  );
            this.jsonList.add(this.json);
            this.jsonTop = this.jsonList;
            this.json = new LinkedHashMap<>(  );
        }
        else {
            this.jsonList.add(this.json);
            this.json = new LinkedHashMap<>(  );
        }

        this.json.put("type", name);

        return this;
    }

    /**
     * Adds a name and value property to the component.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     *
     * @return The builder instance.
     */
    APSWebManagerGUIBuilder p(String name, Object value) {

        this.json.put(name, value);

        return this;
    }

    /**
     * Adds a set of children. Create them by creating a new builder.
     *
     * @param children A list of child objects to add.
     *
     * @return The builder instance.
     */
    APSWebManagerGUIBuilder children(List children) {

        this.json.put("children", children);

        return this;
    }

    /**
     * @return The builder result. It will either be a Map<String, Object> or a List<Map<String, Object>>.
     */
    Object result() {
        if (this.jsonList == null) {
            return this.json; // Should be same as this.jsonTop in this case!
        }

        this.jsonList.add(this.json);
        return this.jsonTop;
    }

    /**
     * @return result cast to a Map.
     */
    @SuppressWarnings( "unchecked" )
    Map<String, Object> resultAsMap() {
        return (Map<String, Object>)result();
    }

    /**
     * @return result cast to a List.
     */
    @SuppressWarnings( "unchecked" )
    List<Map<String, Object>> resultAsList() {
        return (List<Map<String, Object>>)result();
    }
}
