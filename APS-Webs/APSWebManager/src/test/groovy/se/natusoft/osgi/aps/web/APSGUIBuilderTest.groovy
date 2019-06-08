package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class APSGUIBuilderTest {

    @Test
    void guiBuilderTest() {

        Map<String, Serializable> gui = new APSGUIBuilder().
                comp( "aps-layout" ).
                id( "page" ).attr( "name", "page" ).attr( "orientation", "vertical" ).
                attr( "borderStyle", "1px solid black" ).attr( "border", false ).
                children()
                .toMap()
    }
}
