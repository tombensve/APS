package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import org.junit.Test
import se.natusoft.osgi.aps.core.lib.StructMap

@CompileStatic
class StructMapTest {

    private StructMap structMap = new StructMap(
            [
                    header: [
                            type      : "service",
                            address   : "aps.admin.web",
                            classifier: "public",
                            enabled   : true
                    ],
                    body  : [
                            action: "get-webs"
                    ],
                    reply : [
                            webs: [
                                    [
                                            name: "ConfigAdmin",
                                            url : "http://localhost:8080/aps/ConfigAdminWeb",
                                    ],
                                    [
                                            name: "RemoteServicesAdmin",
                                            url : "https://localhost:8080/aps/RemoteSvcAdmin"
                                    ]
                            ]
                    ]
            ] as Map<String, Object>
    )

    @Test
    void testKeys() throws Exception {

        List<String> validValues = [
                "header.type",
                "header.address",
                "header.classifier",
                "header.enabled",
                "body.action",
                "reply.webs.[2].name",
                "reply.webs.[2].url"
        ]
        structMap.withStructPath { String path ->
            println "${path}"
            assert validValues.contains( path )
        }
    }

    @Test
    void testValues() throws Exception {

        structMap.lookupr( "header.type" ) { Object r -> assert r.toString() == "service" }
        structMap.lookupr( "header.address" ) { Object r -> assert r.toString() == "aps.admin.web" }
        structMap.lookupr( "header.classifier" ) { Object r -> assert r.toString() == "public" }
        structMap.lookupr( "body.action" ) { Object r -> assert r.toString() == "get-webs" }
        structMap.lookupr( "reply.webs.[0].name" ) { Object r -> assert r.toString() == "ConfigAdmin" }
        structMap.lookupr( "reply.webs.[1].name" ) { Object r -> assert r.toString() == "RemoteServicesAdmin" }
        structMap.lookupr( "reply.webs.[1].url" ) { Object r -> assert r.toString() == "https://localhost:8080/aps/RemoteSvcAdmin" }
        structMap.lookupr( "reply.webs.[*]" ) { int size -> assert size == 2 }

        // This works when it is a Map being returned. It will however not work for a List.
        structMap.lookupr( "header" ) { Map<String, Object> map ->
            StructMap smap = new StructMap( map )

            smap.lookupr( "address" ) { Object r -> assert r.toString() == "aps.admin.web" }
        }

        for ( int i = 0; i < (int) structMap.lookup( "reply.webs.[*]" ); i++ ) {
            println "${structMap.lookup("reply.webs.[$i].name")}"
        }

    }

    @Test
    void testSetValue() throws Exception {
        structMap.provide( "body.name.general", "qwerty" )
        structMap.lookupr( "body.name.general" ) { Object r -> assert r.toString() == "qwerty" }

        structMap.provide( "header.type", "qaz" )
        structMap.lookupr( "header.type" ) { Object r -> assert r.toString() == "qaz" }

        structMap.provide( "reply.webs.[1].url", "http://www.google.com/" )
        structMap.lookupr( "reply.webs.[1].url" ) { Object r -> assert r.toString() == "http://www.google.com/" }

        structMap.provide( "tommy.test.arr.[1].val", "qwerty" )
        structMap.lookupr( "tommy.test.arr.[1].val" ) { Object r -> assert r.toString() == "qwerty" }

        structMap.withStructPath { String path ->
            println "${path}"
        }
    }

}
