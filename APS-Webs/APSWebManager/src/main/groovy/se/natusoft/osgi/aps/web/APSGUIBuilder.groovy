package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class APSGUIBuilder {

    private Map<String, Serializable> gui = [:]

    private Map<String, Serializable> current = gui

    private Map<String, Serializable> parent = null

    APSGUIBuilder comp(String name) {
        current["type"] = name
        this
    }

    APSGUIBuilder id(String id) {
        attr("id", id)
    }

    APSGUIBuilder attr(String name, Serializable value) {
        current[name] = value
        this
    }

    APSGUIBuilder children() {
        List<Map<String,Serializable>> children = []
        this
    }

    APSGUIBuilder endChildren() {
        endSub(  )
    }

    APSGUIBuilder subEntry(String name) {
        Map<String, Serializable> sub = [:]
        current[name] = sub
        parent = current
        current = sub
        this
    }

    APSGUIBuilder endSub() {
        current = parent
        parent = null
        this
    }

    APSGUIBuilder headers() {
        Map<String, Serializable> headers = [:]
        current["headers"] = headers
        parent = current
        current = headers
        this
    }

    APSGUIBuilder incomingRoutes(String incoming) {
        Map<String, Serializable> routing = current["routing"] as Map<String, Serializable>
        if (routing == null) {
            routing = [:]
            current["routing"] = routing
        }
        routing["incoming"] = incoming
        this
    }

    APSGUIBuilder outgoingRoutes(String outgoing) {
        Map<String, Serializable> routing = current["routing"] as Map<String, Serializable>
        if (routing == null) {
            routing = [:]
            current["routing"] = routing
        }
        routing["outgoing"] = outgoing
        this
    }

    Map<String, Serializable> toMap() {
        this.gui
    }
}
