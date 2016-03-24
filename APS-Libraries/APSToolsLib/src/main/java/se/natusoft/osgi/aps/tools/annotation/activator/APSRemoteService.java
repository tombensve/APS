package se.natusoft.osgi.aps.tools.annotation.activator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will make the service externally available through APSExternalProtocolExtender and provided protocols and transports.
 * This is a synonym to @APSExternalizable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface APSRemoteService {}
