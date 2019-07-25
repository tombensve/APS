package se.natusoft.osgi.aps.core.lib

import org.junit.Test

class MapJsonLoaderTest {

    @Test
    void testLoad() {
        Map<String, Object> json = MapJsonLoader.loadMapJson( "json/top.json", this.class.classLoader )

        assert json['include']['Num'] == 34
    }
}
