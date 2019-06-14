package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class APSGUIBuilderTest {

    private class TestGUIProvider extends GUIProvider {

        Map<String, Object> testbuildGUIUsingModels() {
            buildGUIUsingModels()
        }
    }

    @Test
    void guiBuilderTest() {

        Map<String, Serializable> gui = new APSGUIBuilder().
                comp( "aps-layout" ).
                id( "page" ).attr( "name", "page" ).attr( "orientation", "vertical" ).
                attr( "borderStyle", "1px solid black" ).attr( "border", false ).
                children()
                .toMap()
    }

    @Test
    void exampleGuiBuilderTest() {
        TestGUIProvider tgp = new TestGUIProvider()
        Map<String, Object> gui = tgp.testbuildGUIUsingModels(  )

        println "${gui}"
    }
}
