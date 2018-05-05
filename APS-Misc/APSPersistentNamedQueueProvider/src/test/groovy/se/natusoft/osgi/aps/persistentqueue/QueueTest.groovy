package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.misc.queue.APSNamedQueueService
import se.natusoft.osgi.aps.api.misc.queue.APSQueue

import se.natusoft.osgi.aps.exceptions.APSIOTimeoutException
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertEquals

@CompileStatic
@TypeChecked
class QueueTest extends OSGIServiceTestTools {

    private File fsRoot

    @Before
    void setup() {
        URL testFileURL = getClass().getClassLoader().getResource("test.file")
        String path = testFileURL.toString()
        if (path.startsWith("file:")) {
            path = path.substring 5

            this.fsRoot = new File(path)
            this.fsRoot = this.fsRoot.parentFile.parentFile.parentFile
            this.fsRoot = new File(this.fsRoot, "target")
            this.fsRoot = new File(this.fsRoot, "fsroot")
            this.fsRoot.mkdirs()

            println "${this.fsRoot}"
        } else {
            println "WARNING: Test resource URL did not start with 'file:'! This test cannot handle that so test is skipped!"
        }
    }

    @Test
    void queueWriteReadSequential() throws Exception {
        Thread.sleep(2000)

        System.properties."aps.filesystem.root" = this.fsRoot.absolutePath

        // First deploy the aps-filesystem-service which our service uses to store the queues.
        deploy 'aps-filesystem-service-provider' with new APSFilesystemActivator() from 'se.natusoft.osgi.aps', 'aps-filesystem-service-provider', '1.0.0'

        // Then deploy our service to test from target/classes.
        deploy 'aps-persistent-named-queue-service-provider' with new APSActivator() from "APS-Misc/APSPersistentNamedQueueProvider/target/classes"

        try {
            with_new_bundle "queue-write-and-read-test", { BundleContext context ->

                APSServiceTracker<APSNamedQueueService> queueServiceTracker =
                        new APSServiceTracker<>(context, APSNamedQueueService.class, "10 seconds")
                queueServiceTracker.start()

                APSNamedQueueService namedQueueService = queueServiceTracker.getWrappedService()

                // Write
                println "Starting write ..."
                APSQueue queue = namedQueueService.createQueue "testQ"

                BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test.file")))
                br.eachLine { String line -> // https://youtrack.jetbrains.com/issue/IDEA-153785
                    queue.push line.bytes
                }
                br.close()
                println "Write done: ${queue.size()} entries!"

                // Validate write
                assertEquals 9943, queue.size()

                // Read
                println "Starting read ..."
                int read = 0
                while (!queue.empty) {
                    String item = new String(queue.pull(1000))
                    ++read
                    //println "${item}"
                }
                println "Read done: ${read} entries!"

                // Validate read
                assertEquals 9943, read
                assertEquals 0, queue.size()

                // Cleanup
                queue.release()
                queueServiceTracker.stop(context)
            }
        }
        finally {
            shutdown()
        }
    }

    @Test
    void queueWriteReadParallel() throws Exception {

        System.properties."aps.filesystem.root" = this.fsRoot.absolutePath

        // First deploy the aps-filesystem-service which our service uses to store the queues.
        deploy 'aps-filesystem-service-provider' with new APSFilesystemActivator() from 'se.natusoft.osgi.aps', 'aps-filesystem-service-provider', '1.0.0'

        // Then deploy our service to test from target/classes.
        deploy 'aps-persistent-named-queue-service-provider' with new APSActivator() from "APS-Misc/APSPersistentNamedQueueProvider/target/classes"

        try {
            with_new_bundle "queue-write-and-read-test", { BundleContext context ->

                APSServiceTracker<APSNamedQueueService> queueServiceTracker =
                        new APSServiceTracker<>(context, APSNamedQueueService.class, "10 seconds")
                queueServiceTracker.start()

                APSNamedQueueService namedQueueService = queueServiceTracker.getWrappedService()

                // Write
                println "Starting write ..."
                APSQueue queue = namedQueueService.createQueue "testQ"

                Thread.start {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test.file")))
                    br.eachLine { String line -> // https://youtrack.jetbrains.com/issue/IDEA-153785
                        queue.push line.bytes
                    }
                    br.close()
                    println "Write done: ${queue.size()} entries left in queue!"
                }

                // Read
                println "Starting read ..."
                int read = 0
                while (true) {
                    try {
                        // Make this timeout here longer if the test fails. That would also mean that you have
                        // a really slow machine!
                        queue.pull(3000)
                        ++read
                    }
                    catch (APSIOTimeoutException aiote) {
                        break
                    }
                }
                println "Read done: ${read} entries!"

                // Validate read
                assertEquals 9943, read
                assertEquals 0, queue.size()

                // Cleanup
                queue.release()
                queueServiceTracker.stop(context)
            }
        }
        finally {
            shutdown()
        }
    }
}
