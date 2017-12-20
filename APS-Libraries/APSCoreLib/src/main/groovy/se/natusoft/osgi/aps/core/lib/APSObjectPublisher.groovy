package se.natusoft.osgi.aps.core.lib

import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.NotUsed
import se.natusoft.osgi.aps.api.pubcon.APSConsumer
import se.natusoft.osgi.aps.api.util.APSMeta

/**
 * This makes use of an APSServiceTracker to track and call APSConsumer services.
 *
 * This is a tool to make it easy to publish data to consumer services.
 *
 * This follows the reactive pattern.
 *
 * Rather than having a service with publish method and a method for registering consumers, this makes
 * more use of OSGi to handle this. A Consumer implements APSConsumer&lt;T&gt; and publishes it as an OSGi
 * service with some meta data in the properties that is used by this tool to find the correct consumers to
 * deliver to.
 *
 * Do note that this is not for publishing a stream of messages!! This reuses the same API because the API
 * fits and makes sense. This publishes a single instance of "published" on start(context) and revokes it
 * on stop().
 *
 * The Published object can be an APSConsumed<RealPublished> in which case a consumer can tell the publisher
 * that it is no longer interested in the published object.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class APSObjectPublisher<Published> {

    //
    // Constants
    //

    private static final APSMeta OBJECT_PUBLISHED = APSMeta.with( [ "status": APSMeta.OBJECT_PUBLISHED_STATUS ] )
    private static final APSMeta OBJECT_UPDATED = APSMeta.with( [ "status": APSMeta.OBJECT_UPDATED_STATUS ] )
    private static final APSMeta OBJECT_REVOKED = APSMeta.with( [ "status": APSMeta.OBJECT_REVOKED_STATUS ] )

    //
    // Properties
    //

    /** The bundle context. */
    BundleContext context

    /** A query in LDAP format as used by OSGi. Should match properties published by target consumers. */
    String consumerQuery = ""

    /** The timeout to find at least one consumer. */
    String timeout = "5 minutes"

    /** This will be called with a ServiceReference. A true result will result in that the consumer is accepted. */
    Closure<Boolean> newConsumerCallback

    //
    // Private Members
    //

    /** For tracking consumers. */
    private APSGServiceTracker<APSConsumer<Published>> consumerTracker

    /** For keeping track of which consumers have received published object. */
    private Map<ServiceReference, APSConsumer> knownConsumers

    /** The published object. */
    private Published published

    //
    // Constructorish
    //

    /**
     * This must be called after setting properties, but before calling publish(...).
     */
    APSObjectPublisher<Published> init() {
        this.knownConsumers = [ : ]

        this.consumerTracker = new APSGServiceTracker<>( context, APSConsumer.class, this.consumerQuery, timeout )

        this.consumerTracker.onServiceAvailable = this.&onServiceAvailableHandler
        this.consumerTracker.onServiceLeaving = this.&onServiceLeavingHandler

        this.consumerTracker.start()

        return this
    }

    //
    // Methods
    //

    /**
     * Starts tracking consumers.
     *
     * @param context The bundle context of the caller.
     */
    void publish( @NotNull Published published ) {
        this.published = published

        this.consumerTracker.g_withAllAvailableServices { APSConsumer<Published> consumer ->
            // I believe that the requirement of "as Published" is an IDEA warning bug. this.published is of type Published!
            consumer.apsConsume( this.published as Published, OBJECT_UPDATED )
        }
    }

    /**
     * Stops tracking consumers.
     */
    void revoke() {
        this.consumerTracker.g_withAllAvailableServices { APSConsumer<Published> consumer ->
            try {
                //noinspection GroovyAssignabilityCheck
                consumer.apsConsume( this.published, OBJECT_REVOKED )
            }
            catch ( Exception e ) {
                // We don't have access to LogService nor APSLogger here!
                System.err.println( e.toString() )
            }
        }
        this.consumerTracker.stop()
        this.knownConsumers = null
        this.published = null
    }

    /**
     * Called by the consumer tracker when there is a new consumer.
     *
     * @param consumer The new consumer.
     * @param serviceReference The reference of the consumer.
     */
    private void onServiceAvailableHandler( @NotNull APSConsumer<Published> consumer,
                                            @NotNull ServiceReference serviceReference ) {
        if ( !this.knownConsumers.containsKey( serviceReference ) ) {
            if ( this.newConsumerCallback == null || this.newConsumerCallback( serviceReference ) ) {
                //noinspection GroovyAssignabilityCheck
                consumer.apsConsume( this.published, OBJECT_PUBLISHED )
                this.knownConsumers[ serviceReference ] = consumer
            }
        }
    }

    /**
     * Called by the consumer tracker when a consumer is leaving.
     *
     * @param serviceReference The service reference of the consumer.
     * @param serviceAPI The service API of the consumer. Not used!
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    private synchronized void onServiceLeavingHandler( @NotNull ServiceReference serviceReference,
                                                       @NotUsed Class serviceAPI ) {
        if ( this.knownConsumers.containsKey( serviceReference ) ) {
            this.knownConsumers.remove( serviceReference )
        }
    }
}
