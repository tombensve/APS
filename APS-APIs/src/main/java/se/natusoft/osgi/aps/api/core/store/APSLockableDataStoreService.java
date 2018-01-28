package se.natusoft.osgi.aps.api.core.store;

import se.natusoft.osgi.aps.api.core.APSLockable;

/**
 * A convenience providing both APSDataStoreService and APSLockable in one API.
 */
public interface APSLockableDataStoreService extends APSDataStoreService, APSLockable {}
