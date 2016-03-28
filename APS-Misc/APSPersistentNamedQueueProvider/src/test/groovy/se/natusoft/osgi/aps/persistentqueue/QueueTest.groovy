package se.natusoft.osgi.aps.persistentqueue

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*
import se.natusoft.osgi.aps.api.misc.queue.APSNamedQueueService
import se.natusoft.osgi.aps.api.misc.queue.APSQueue
import se.natusoft.osgi.aps.core.filesystem.APSFilesystemActivator
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 *
 */
class QueueTest extends OSGIServiceTestTools {

    private File fsRoot

    @Before
    void setup() {
        URL testFileURL = getClass().getClassLoader().getResource("test.file")
        String path = testFileURL.toString()
        if (path.startsWith("file:")) {
            path = path.substring(5)

            this.fsRoot = new File(path)
            this.fsRoot = this.fsRoot.parentFile.parentFile.parentFile
            this.fsRoot = new File(this.fsRoot, "target")
            this.fsRoot = new File(this.fsRoot, "fsroot")
            this.fsRoot.mkdirs()

            println "${this.fsRoot}"
        }
        else {
            println("WARNING: Test resource URL did not start with 'file:'! This test cannot handle that so test is skipped!")
        }
    }

    @Test
    void createQueue() throws Exception {
        System.setProperty("aps.filesystem.root", this.fsRoot.absolutePath)

        // First deploy the aps-filesystem-service which our service uses to store the queues.
        deploy 'aps-filesystem-service-provider' with new APSFilesystemActivator() from 'se.natusoft.osgi.aps', 'aps-filesystem-service-provider', '1.0.0'

        // Then deploy our service to test from target/classes.
        deploy 'aps-persistent-named-queue-service-provider' with new APSActivator() from "APS-Misc/APSPersistentNamedQueueProvider/target/classes"

        // And then deploy the below one class bundle that actually reads all lines in test.file and writes them to a queue.
        // Since the bundles start is not threaded it will run on deploy and it will not return until done. This is the lazy
        // way to run it since it will get our APSNamedQueueService implementation automatically injected.
        deploy 'queue-writer-bundle' with new APSActivator() using '/se/natusoft/osgi/aps/persistentqueue/QueueWriteSvc.class'

        File qd = new File(this.fsRoot, "aps-persistent-named-queue-service-provider")
        qd = new File(qd, "testQ")

        assertEquals(9944, qd.listFiles().length)

        deploy 'queue-reader-bundle' with new APSActivator() using '/se/natusoft/osgi/aps/persistentqueue/QueueReadSvc.class'

        assertEquals(1, qd.listFiles().length) // only the empty index should be there.

        shutdown()
    }
}

class QueueWriteSvc {

    @OSGiService
    private APSNamedQueueService queueSvc

    @BundleStart
    void createAndWriteQueue() {
        APSQueue queue = this.queueSvc.createQueue("testQ")
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test.file")))
        String line = br.readLine()
        while (line != null) {
            queue.push(line.bytes)
            line = br.readLine()
        }
        br.close()

        queue.release()
    }
}

class QueueReadSvc {

    @OSGiService
    private APSNamedQueueService queueSvc

    @BundleStart
    void getAndReadQueue() {
        APSQueue queue = this.queueSvc.getQueue("testQ")

        while (!queue.empty) {
            String item = new String(queue.pull())
            //println "${item}"
        }

        queue.release()
    }
}
