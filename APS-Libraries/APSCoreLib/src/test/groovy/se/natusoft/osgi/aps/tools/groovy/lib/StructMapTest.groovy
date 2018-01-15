package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Ignore
import org.junit.Test
import se.natusoft.osgi.aps.core.lib.StructMap

@CompileStatic
@TypeChecked
class StructMapTest {

    private StructMap structMap = new StructMap<>(
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
    ) as StructMap

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
    @Ignore
    void testValues() throws Exception {

        structMap.lookup( "header.type" ) { Object r -> assert r.toString() == "service" }
        structMap.lookup( "header.address" ) { Object r -> assert r.toString() == "aps.admin.web" }
        structMap.lookup( "header.classifier" ) { Object r -> assert r.toString() == "public" }
        structMap.lookup( "body.action" ) { Object r -> assert r.toString() == "get-webs" }
        structMap.lookup( "reply.webs.[0].name" ) { Object r -> assert r.toString() == "ConfigAdmin" }
        structMap.lookup( "reply.webs.[1].name" ) { Object r -> assert r.toString() == "RemoteServicesAdmin" }
        structMap.lookup( "reply.webs.[1].url" ) { Object r -> assert r.toString() == "https://localhost:8080/aps/RemoteSvcAdmin" }

    }

    @Test
    @Ignore
    void testSetValue() throws Exception {
        structMap.provide( "body.name.general", "qwerty" )
        structMap.lookup("body.name.general" ) { Object r -> assert r.toString() == "qwerty" }

        structMap.provide( "header.type", "qaz")
        structMap.lookup("header.type" ) { Object r -> assert r.toString() == "qaz" }

        structMap.provide( "reply.webs.[1].url", "http://www.google.com/")
        structMap.lookup("reply.webs.[1].url" ) { Object r -> assert r.toString() == "http://www.google.com/" }

        structMap.provide("tommy.test.arr.[1].val", "qwerty")
        structMap.lookup("tommy.test.arr.[1].val" ) { Object r -> assert r.toString() == "qwerty" }

        structMap.withStructPath { String path ->
            println "${path}"
        }
    }

}
