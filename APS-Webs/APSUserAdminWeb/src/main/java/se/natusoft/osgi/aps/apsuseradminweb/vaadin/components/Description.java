package se.natusoft.osgi.aps.apsuseradminweb.vaadin.components;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.EditorIdentifier;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HTMLFileLabel;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;

/**
 * The center view description.
 */
public class Description extends HTMLFileLabel {

    public static DescriptionHandler DESCRIPTION_VIEW = new DescriptionHandler();

    private Description() {
        super("html/description.html", APSTheme.THEME, Description.class.getClassLoader());
    }

    /**
     * This is a component that delivers a layout with margins containing the description, and implementing
     * all interfaces needed to use it as a menu component handler and a default main app view.
     */
    private static class DescriptionHandler extends VerticalLayout implements ComponentHandler, EditorIdentifier {

        /**
         * Creates a new DescriptionHandler.
         */
        public DescriptionHandler() {
            setMargin(true);
            this.setStyleName(CSS.APS_CONTENT_PANEL);

            addComponent(new Description());
        }

        /**
         * @return The component that should handle the item.
         */
        @Override
        public AbstractComponent getComponent() {
            return this;
        }

        @Override
        public String getEditorId() {
            return "description";
        }
    }

}
