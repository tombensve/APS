package se.natusoft.osgi.aps.core.lib

import org.junit.Test

class MapJsonLoaderTest {

    @Test
    void testLoad() {
        Map<String, Object> json = MapJsonLoader.loadMapJson( "json/top.json", this.class.classLoader )

        // Include
        assert json['include']['Num'] == 34

        // Recursive include
        assert json['include']['qazwsx']['qaz'] == "QWERTY"
    }
}
