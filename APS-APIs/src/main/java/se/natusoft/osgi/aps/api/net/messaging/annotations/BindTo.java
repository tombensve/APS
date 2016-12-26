package se.natusoft.osgi.aps.api.net.messaging.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a Netty ChannelInboundHandler to a topic.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindTo {

    /**
     * The topic to bind to.
     */
    String topic() default "";

}
