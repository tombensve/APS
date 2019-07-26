package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class MapJsonLoaderTest {

    @Test
    void testLoad() {
        Map<String, Object> json = MapJsonLoader.loadMapJson( "json/top.json", this.class.classLoader )

        // Include
        assert json['include']['Num'] == 34

        // Recursive include
        assert json['include']['qazwsx']['qaz'] == "QWERTY"

        // Verify expansion of named rule reference.
        assert json['Named rule'] == "?[a-zA-Z 0-9.]*\$"
    }
}
