package se.natusoft.osgi.aps.net.vertx

import org.junit.Test

class GroovyClosureVerification {

    /**
     * I failed to find the answer to this using google, so I did a test.
     *
     * Even though the closure does not take the a or b as argument, but only references the method argument,
     * and by the time the closure executes the method have been called 3 times with 3 different arguments and
     * returned from each method call. Each invocation of the closure however produces the correct a & b. So the
     * referenced variables must be attached to the closure at instance at creation.
     **/
    @Test
    void closureVerification() {
        doClosure( "qwerty", 45 )
        doClosure( "asdf", 32 )
        doClosure( "zxcv", 98 )

        Thread.sleep( 1500 )
    }

    static void doClosure( String a, int b ) {
        def testClosure = {
            println "a='${a}', b='${b}'"
        }

        Thread.start {
            Thread.sleep( 1000 )
            testClosure()
        }
    }
}
