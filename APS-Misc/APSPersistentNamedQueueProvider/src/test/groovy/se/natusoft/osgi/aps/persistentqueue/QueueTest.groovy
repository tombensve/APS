package se.natusoft.osgi.aps.persistentqueue

import org.junit.Before
import org.junit.Test

/**
 *
 */
class QueueTest {

    @Before
    void setup() {
        URL testFileURL = getClass().getClassLoader().getResource("test.file")
        println "${testFileURL}"
    }

    @Test
    void createQueue() throws Exception {

    }
}
