package se.natusoft.osgi.aps.api.core.config.model.admin;

/**
 * This represents an abstract reference to a config object or value.
 */
public interface APSConfigReference {

    /**
     * Returns true if this reference is empty.
     */
    public boolean isEmpty();

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
    public APSConfigReference _(APSConfigValueEditModel cvem);

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
    public APSConfigReference _(APSConfigValueEditModel cvem, int index);

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
    public APSConfigReference _(int index);

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
    public APSConfigReference _(APSConfigEnvironment configEnvironment);

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
    public APSConfigReference configEnvironment(APSConfigEnvironment configEnvironment);

    /**
     * Creates a new reference with an APSConfig*EditModel representing a single value added to the
     * reference. You need to provide these all the way upp to the final value you want to reference.
     *
     * @param cvem A ConfigValueEditModel instance. Can also be the APSConfigEditModel subclass.
     *
     * @return A reference to a specific configuration value. Thereby the last cvem should always
     * be an APSConfigValueEditModel.
     */
    public APSConfigReference editModel(APSConfigValueEditModel cvem);

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
    public APSConfigReference editModel(APSConfigValueEditModel cvem, int index);

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
    public APSConfigReference index(int index);

    /**
     * Returns the last APSConfigValueEditModel in the chain, which should be the one that points to
     * the actual value or object.
     */
    public APSConfigValueEditModel getConfigValueEditModel();

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
    public String getDefaultValue();

    /**
     * Returns the previously provided configuration environment. If you have added new edit models
     * after providing a configuration model then this will return null! Configuration environments
     * should only be provided in the end when referencing values.
     */
    public APSConfigEnvironment getConfigEnvironment();

    /**
     * Returns the index of the tip of the reference.
     */
    public int getIndex();

    /**
     * Returns a copy of this instance.
     */
    public APSConfigReference copy();

}
