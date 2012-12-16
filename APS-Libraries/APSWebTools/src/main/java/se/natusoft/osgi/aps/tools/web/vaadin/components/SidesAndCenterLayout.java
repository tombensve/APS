/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-09-18: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.components;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * This is a layout that only accepts 5 components at 5 positions: left, top, center, bottom, right.
 * <p/>
 * The layout looks like this:
 * <pre>
 *     +------+----------------+------+
 *     |      |                |      |
 *     |      +----------------+      |
 *     |      |                |      |
 *     |      |                |      |
 *     |      |                |      |
 *     |      +----------------+      |
 *     |      |                |      |
 *     +------+----------------+------+
 * </pre>
 * You set the different components with "setLeft(...), setTop(...), ...". All positions are optional, and you don't have to set any,
 * but that would be pointless.
 * <p/>
 * When you have set all your components the doLayout() method must be called, and it cannot be called before that. If you fail to
 * call doLayout() there will be an exception when rendering.
 */
public class SidesAndCenterLayout extends HorizontalLayout {
    //
    // Private Members
    //

    /** The left component. */
    Component left = null;

    /** The top component. */
    Component top = null;

    /** The center component. */
    private Component center = null;

    /** The bottom component. */
    private Component bottom = null;

    /** The right component. */
    private Component right = null;

    /** Set when doLayout() is called. */
    private boolean layedOut = false;

    //
    // Constructors
    //

    /**
     * Creates a new SidesAndCenterLayout.
     */
    public SidesAndCenterLayout() {
        this.setMargin(false);
    }

    //
    // Methods
    //

    /**
     * Sets the component for the left side.
     *
     * @param left The component to set.
     */
    public void setLeft(Component left) {
        this.left = left;
    }

    /**
     * @return The left component or null if none.
     */
    public Component getLeft() {
        return this.left;
    }

    /**
     * Sets the component for the top.
     *
     * @param top The component to set.
     */
    public void setTop(Component top) {
        this.top = top;
    }

    /**
     * @return The top component or null if none.
     */
    public Component getTop() {
        return this.top;
    }

    /**
     * Sets the component for the center.
     *
     * @param center The component to set.
     */
    public void setCenter(Component center) {
        this.center = center;
    }

    /**
     * @return The center component or null if none.
     */
    public Component getCenter() {
        return this.center;
    }

    /**
     * Sets the component for the bottom.
     *
     * @param bottom The component to set.
     */
    public void setBottom(Component bottom) {
        this.bottom = bottom;
    }

    /**
     * @return The bottom component or null if none.
     */
    public Component getBottom() {
        return this.bottom;
    }


    /**
     * Sets the component for the right side.
     *
     * @param right The component to set.
     */
    public void setRight(Component right) {
        this.right = right;
    }

    /**
     * @return The right component or null if none.
     */
    public Component getRight() {
        return right;
    }

    /**
     * Does the actual layout adding the provided component to the layout. This must be called after the components have been set.
     * <p/>
     * It starts by removing all components so if this has already been setup and this method called, it can be called again with new
     * components added or components replaced with other components.
     */
    public void doLayout() {
        removeAllComponents();

        setSizeFull();

        if (this.left != null) {
            this.left.setHeight("100%");
            this.left.setWidth(null);
            addComponent(left);
        }

        VerticalLayout middleLayout = new VerticalLayout();
        middleLayout.setMargin(false);
        middleLayout.setSizeFull();
        addComponent(middleLayout);
        setExpandRatio(middleLayout, 1.0f);

        if (this.top != null) {
            this.top.setWidth("100%");
            this.top.setHeight(null);
            middleLayout.addComponent(this.top);
        }

        if (this.center != null) {
            this.center.setWidth("100%");
            this.center.setHeight("100%");
            middleLayout.addComponent(this.center);
            middleLayout.setExpandRatio(this.center, 1.0f);
        }

        if (this.bottom != null) {
            this.bottom.setWidth("100%");
            this.bottom.setHeight(null);
            middleLayout.addComponent(this.bottom);
        }

        if (this.right != null) {
            this.right.setHeight("100%");
            this.right.setWidth(null);
            addComponent(this.right);
        }

        this.layedOut = true;
    }

    /**
     * This is only here to validate that doLayout() have been called before painting is done.
     *
     * @param target
     * @throws PaintException
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (!this.layedOut) {
            throw new PaintException("The doLayout() method has not been called on one of the SidesAndCenterLayout objects!");
        }
        super.paintContent(target);
    }
}
