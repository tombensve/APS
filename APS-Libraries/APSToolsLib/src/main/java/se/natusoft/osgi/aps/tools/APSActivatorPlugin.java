package se.natusoft.osgi.aps.tools;

import java.lang.reflect.Field;

/**
 * A service API to implement and publish to be able to plugin to APSActivator.
 *
 * APSActivator will look for all published instances of this service and call them.
 */
@SuppressWarnings("unused")
public interface APSActivatorPlugin {

    /**
     * For the plugin to interact with the activator.
     */
    interface ActivatorInteraction {

        /**
         * Adds an instance to manage.
         *
         * @param instance The instance to add.
         * @param forClass The class of the instance to receive this 'instance'.
         * @param forField The field in that class to receive this 'instance'.
         */
        void addManagedInstance(Object instance, Class forClass, Field forField);
    }

    /**
     * When APSActivator analyzes each class of the bundle it will also pass the class to this method.
     *
     * @param bundleClass The analyzed class.
     */
    void analyseBundleClass(ActivatorInteraction activatorInteraction, Class bundleClass);
}
