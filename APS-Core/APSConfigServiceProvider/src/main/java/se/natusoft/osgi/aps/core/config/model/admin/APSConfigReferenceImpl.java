package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

import static se.natusoft.osgi.aps.core.config.model.StaticUtils.*;

import java.util.LinkedList;

/**
 * This represents an abstract reference to a config value or a config object.
 */
public class APSConfigReferenceImpl implements APSConfigReference {

    //
    // Private Members
    //

    /** We use these to build a complete key. */
    private LinkedList<ConfigPartKey> keys = new LinkedList<>();

    /** The configuration environment for the reference. Only needed for config env specific values. */
    private APSConfigEnvironment configEnvironment = null;

    /** The full edit model added to this instance. For previous only its key is kept. */
    private APSConfigValueEditModel last = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigReferenceImpl.
     */
    public APSConfigReferenceImpl() {}

    /**
     * Copy constructor.
     *
     * @param orig The original to copy.
     */
    private APSConfigReferenceImpl(APSConfigReference orig) {
        for (ConfigPartKey cpk : ((APSConfigReferenceImpl)orig).keys) {
            this.keys.add(cpk.copy());
        }

        this.configEnvironment = ((APSConfigReferenceImpl)orig).configEnvironment;
        this.last = ((APSConfigReferenceImpl)orig).last;
    }

    //
    // Methods
    //

    /**
     * Returns a copy of this instance.
     */
    @Override
    public APSConfigReference copy() {
        return new APSConfigReferenceImpl(this);
    }

    /**
     * Returns true if this reference is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.keys.isEmpty();
    }

    /**
     * Returns a new reference with the specified index.
     *
     * __NOTE__: that the index refers to the current tip of the reference. It can be called multiple
     * times which will return a new reference with only the index changed. However if a new reference
     * with a new edit model is created (_(APSConfigValueEditModel)) the old index gets locked and the
     * call to _(int) on the new reference will set the index for the new model which represents something
     * one branch down in the configuration tree.
     *
     * @param index The index to get reference for.
     */
    @Override
    public APSConfigReference index(int index) {
        APSConfigReferenceImpl ref = toImpl(copy());
        ref.keys.getLast().index(index);
        return ref;
    }

    /**
     * Creates a new reference with an APSConfig*EditModel representing a single value added to the
     * reference. You need to provide these all the way upp to the final value you want to reference.
     *
     * @param cvem A ConfigValueEditModel instance. Can also be the APSConfigEditModel subclass.
     *
     * @return A reference to a specific configuration value. Thereby the last cvem should always
     * be an APSConfigValueEditModel.
     */
    @Override
    public APSConfigReference editModel(APSConfigValueEditModel cvem) {
        APSConfigReferenceImpl ref = toImpl(copy());
        ref.last = cvem;
        ref.keys.add(((APSConfigValueEditModelImpl)cvem).getKey().copy());
        return ref;
    }

    /**
     * Creates a new reference with an APSConfig*EditModel representing a single value added to the
     * reference. You need to provide these all the way upp to the final value you want to reference.
     *
     * @param cvem  A ConfigValueEditModel instance. Can also be the APSConfigEditModel subclass.
     * @param index The index of the value for this APSConfig*EditModel if it represents a list.
     *              You should only use this variant if the model represents a list (returns true
     *              on isMany()).
     *
     * @return A reference to a specific configuration value. Thereby the last cvem should always
     * be an APSConfigValueEditModel.
     */
    @Override
    public APSConfigReference editModel(APSConfigValueEditModel cvem, int index) {
        APSConfigReferenceImpl ref = toImpl(copy());
        ref.last = cvem;
        ref.keys.add(((APSConfigValueEditModelImpl)cvem).getKey().copy().index(index));
        return ref;
    }

    /**
     * Creates a new reference with an APSConfig*EditModel representing a single value added to the
     * reference. You need to provide these all the way upp to the final value you want to reference.
     *
     * __NOTE__: This is an alias for _editModel(APSConfigValueEditModel)_.
     *
     * @param cvem A ConfigValueEditModel instance. Can also be the APSConfigEditModel subclass.
     *
     * @return A reference to a specific configuration value. Thereby the last cvem should always
     * be an APSConfigValueEditModel.
     */
    @Override
    public APSConfigReference _(APSConfigValueEditModel cvem) {
        return editModel(cvem);
    }

    /**
     * Creates a new reference with an APSConfig*EditModel representing a single value added to the
     * reference. You need to provide these all the way upp to the final value you want to reference.
     *
     * __NOTE__: This is an alias for _editModel(APSConfigValueEditModel, int)_.
     *
     * @param cvem  A ConfigValueEditModel instance. Can also be the APSConfigEditModel subclass.
     * @param index The index of the value for this APSConfig*EditModel if it represents a list.
     *              You should only use this variant if the model represents a list (returns true
     *              on isMany()).
     *
     * @return A reference to a specific configuration value. Thereby the last cvem should always
     * be an APSConfigValueEditModel.
     */
    @Override
    public APSConfigReference _(APSConfigValueEditModel cvem, int index) {
        return editModel(cvem, index);
    }

    /**
     * Returns a new reference with the specified index.
     *
     * __NOTE 1__: that the index refers to the current tip of the reference. It can be called multiple
     * times which will return a new reference with only the index changed. However if a new reference
     * with a new edit model is created (_(APSConfigValueEditModel)) the old index gets locked and the
     * call to _(int) on the new reference will set the index for the new model which represents something
     * one branch down in the configuration tree.
     *
     * __NOTE 2__: This is an alias for _index(index)__.
     *
     * @param index The index to get new reference for.
     */
    @Override
    public APSConfigReference _(int index) {
        return index(index);
    }


    /**
     * Returns a new reference also pointing out the specified configuration environment for the
     * referenced value.
     *
     * __NOTE 1__: that you should only do this call when the reference is pointing all the way
     * to a configuration value. It will have no effect otherwise. The suggested way to use
     * references are to build them all the way upp to the value, and then only when using
     * the reference to set or get a value do `...setConfigValue(ref._(configEnv), value);`
     *
     * __NOTE 2__: This is an alias for _configEnvironment(configEnv)_.
     *
     * @param configEnvironment The configuration environment to provide.
     */
    @Override
    public APSConfigReference _(APSConfigEnvironment configEnvironment) {
        return configEnvironment(configEnvironment);
    }

    /**
     * Returns a new reference also pointing out the specified configuration environment for the
     * referenced value.
     *
     * __NOTE__: that you should only do this call when the reference is pointing all the way
     * to a configuration value. It will have no effect otherwise. The suggested way to use
     * references are to build them all the way upp to the value, and then only when using
     * the reference to set or get a value do `...setConfigValue(ref.configEnvironment(configEnv), value);`
     *
     * @param configEnvironment The configuration environment to provide.
     */
    @Override
    public APSConfigReference configEnvironment(APSConfigEnvironment configEnvironment) {
        APSConfigReferenceImpl ref = toImpl(copy());
        ref.configEnvironment = configEnvironment;
        return ref;
    }

    /**
     * Returns the last APSConfigValueEditModel passed to this instance which should be the one that points to
     * the actual value.
     */
    @Override
    public APSConfigValueEditModel getConfigValueEditModel() {
        return this.last;
    }

    /**
     * Returns the previously provided configuration environment. If you have added new edit models
     * after providing a configuration model then this will return null! Configuration environments
     * should only be provided in the end when referencing values.
     */
    @Override
    public APSConfigEnvironment getConfigEnvironment() {
        return this.configEnvironment;
    }

    /**
     * Returns the index of the last part of the key.
     */
    @Override
    public int getIndex() {
        return this.keys.getLast().getIndex();
    }

    /**
     * Returns the default value of the value this reference references. The provided configuration
     * environment is used to get a configuration environment specific value. Thereby you should
     * probably not call this if you have not provided a configuration environment yet.
     *
     * One way to use this is to build a reference up to the value, and then do:
     *
     *     String ce1DefaultValue = myRef._(configEnv1).getDefaultValue();
     *     String ce2DefaultValue = myRef._(configEnv2).getDefaultValue();
     */
    @Override
    public String getDefaultValue() {
        return this.last.getDefaultValue(this.configEnvironment);
    }

    /**
     * Builds and returns the full value key.
     */
    public ConfigValueKey getValueKey() {
        return new ConfigValueKey(
                this.configEnvironment != null ? this.configEnvironment.getName() : "default",
                this.keys
        );
    }

    /**
     * Returns a String representation of this reference.
     */
    @Override
    public String toString() {
        return getValueKey().toString();
    }

}
