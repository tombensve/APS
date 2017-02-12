package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class GroovyEvalTest {

    private static String gs = """
[
  ssl:true,
  trustStoreOptions:[
    path:"/path/to/your/truststore.jks",
    password:"password-of-your-truststore"
  ]
]
"""

    @Test
    void evalTest() {

        Binding binding = new Binding()
        GroovyShell gshell = new GroovyShell(binding)
        Object res = gshell.evaluate(gs)

        println "${res.class}  :  ${res}"
    }
}
