package se.natusoft.osgi.aps.nodeinfo

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.core.platform.service.APSExecutionService
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.util.APSThreadFactory
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Provides an implementation of the APSPlatformService.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-execution-service-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Misc),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.General)
        ]
)
class APSExecutionServiceProvider implements APSExecutionService {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-execution-service-provider")
    private APSLogger logger

    /** This have a thread pool matching number of cores */
    private ExecutorService executor

    //
    // Constructors
    //

    /**
     * Creates a new APSPlatformServiceProvider instance.
     */
    APSExecutionServiceProvider() {}

    //
    // Methods
    //

    /**
     * This gets called after dependency injections are done.
     */
    @Initializer
    void init() {
        this.executor =
                Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors(), new APSThreadFactory( "aps-exec-svc-" ) )
    }


    @BundleStop
    void shutdown() {
        this.executor.shutdownNow()
    }

    /**
     * Submits a job for execution on a thread pool.
     *
     * @param job The job to submit.
     */
    @Override
    void submit( @NotNull APSHandler job ) {
        this.executor.submit {
            try {
                job.handle( null )
            }
            catch ( Exception e ) {
                this.logger.error(e.message, e)
            }
        }
    }

    /**
     * Does the same as submit(job), but in groovy you can do:
     *
     *     this.execSvc << { ... }
     *
     * @param job The job to submit.
     */
    void leftShift(@NotNull APSHandler job) {
        submit(job)
    }

    /**
     * Submits a job for execution on a thread pool.
     *
     * Do note that as a special feature an APSValue is created and passed to both handlers. This means that it is
     * possible to pass a value from one to the other.
     *
     * @param job The job to submit.
     * @param jobDoneHandler The handler to call when the job have finished execution. It will supply a success or fail result.
     *                       fail will only happen if the job threw an exception.
     */
    @Override
    void submit( @NotNull APSHandler job, @NotNull APSHandler<APSResult> jobDoneHandler ) {
        this.executor.submit {
            try {
                job.handle( null )
                jobDoneHandler.handle( APSResult.success( null ) )
            }
            catch ( Exception e ) {
                jobDoneHandler.handle( APSResult.failure( e ) )
            }
        }
    }
}

