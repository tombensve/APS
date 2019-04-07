package se.natusoft.osgi.aps.web.backend.component.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Models components that are of container type having child components.
 */
public class APSContainerComponent extends APSComponent {

    private List<APSComponent> children = new LinkedList<>(  );

    public APSContainerComponent() {
        this.setProperty( "children", children );
    }

    public void addChild(APSComponent component) {
        this.children.add(component);
    }

}
