package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.core.lib.Mapo

@CompileStatic
@TypeChecked
class MapoTest {

    private Mapo mapo = new Mapo<>(
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
    ) as Mapo

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
        mapo.withAllKeys { String key ->
            println "${key}"
            assert validValues.contains( key )
        }
    }

    @Test
    void testValues() throws Exception {
        assert mapo.lookup( "header.type" ).toString() == "service"
        assert mapo.lookup( "header.address" ).toString() == "aps.admin.web"
        assert mapo.lookup( "header.classifier" ).toString() == "public"
        assert mapo.lookup( "body.action" ).toString() == "get-webs"
        assert mapo.lookup( "reply.webs.[0].name") == "ConfigAdmin"
        assert mapo.lookup( "reply.webs.[1].name") == "RemoteServicesAdmin"
        assert mapo.lookup( "reply.webs.[1].url") == "https://localhost:8080/aps/RemoteSvcAdmin"
    }

}
