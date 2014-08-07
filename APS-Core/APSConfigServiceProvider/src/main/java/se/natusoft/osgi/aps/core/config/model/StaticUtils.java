package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigReferenceImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigValueEditModelImpl;

import java.util.Stack;

/**
 * A set of static utility methods.
 */
public class StaticUtils {

    /**
     * Utility to cast interface to implementation in only 6 chars :-).
     *
     * @param model The APSConfigValueEditModel instance to cast.
     */
    public static APSConfigValueEditModelImpl toImpl(APSConfigValueEditModel model) {
        return (APSConfigValueEditModelImpl)model;
    }

    /**
     * Utility to cast interface to implementation in only 6 chars :-).
     *
     * @param model The APSConfigEditModel instance to cast.
     */
    public static APSConfigEditModelImpl toImpl(APSConfigEditModel model) {
        return (APSConfigEditModelImpl)model;
    }

    /**
     * Utility to cast interface to implementation in only 6 chars :-).
     *
     * @param reference The reference to cast.
     */
    public static APSConfigReferenceImpl toImpl(APSConfigReference reference) {
        return (APSConfigReferenceImpl)reference;
    }

    /**
     * Factory to create a APSConfigReference.
     */
    public static APSConfigReference newRef() {
        return new APSConfigReferenceImpl();
    }

    /**
     * After this returns a valid reference is ensured. If the passed ref was not null it is just returned.
     *
     * @param ref The reference to ensure. Can be null in which case a new will be created and returned.
     * @param configValueEditModel The APSConfigValueEditModel for which to create the reference if ref is null.
     *
     * @return A valid reference.
     */
    public static APSConfigReference ensureRef(APSConfigReference ref, APSConfigValueEditModel configValueEditModel) {
        if (ref == null || ref.isEmpty()) {
            if (ref == null) ref = newRef();
            APSConfigValueEditModel current = configValueEditModel;
            Stack<APSConfigValueEditModel> tmpStack = new Stack<>();
            while (current != null) {
                tmpStack.push(current);
                current = current.getParent();
            }
            while (!tmpStack.isEmpty()) {
                APSConfigValueEditModel cvem = tmpStack.pop();
                ref = ref._(cvem);
            }
        }

        return ref;
    }

    /**
     * Ensures non null string. If in string is null then out string is "".
     *
     * @param value The string value to ensure is not null.
     */
    public static String nullSafe(String value) {
        return value != null ? value : "";
    }

}
