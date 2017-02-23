package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class MapJsonDocVerifierTest {

    private Map<String, Object> schema = [
            header_1: [
                    type_1      : "service",
                    address_1   : "?aps\\.admin\\..*",
                    classifier_1: "?public|private"
            ],
            body_1  : [
                    action_1: "get-webs"
            ],
            reply_0: [
                    webs_1: [
                            [
                                    name_1: "?.*",
                                    url_1: "?^https?://.*"
                            ]
                    ]
            ]
    ] as Map<String, Object>

    private MapJsonDocVerifier verifier = new MapJsonDocVerifier( validStructure: schema )

    @Test
    void testFullyCorrect() throws Exception {

        verifier.validate ( [
                header: [
                        type:       "service",
                        address:    "aps.admin.web",
                        classifier: "public"
                ],
                body: [
                        action: "get-webs"
                ],
                reply: [
                        webs: [
                                [
                                        name: "ConfigAdmin",
                                        url:  "http://localhost:8080/aps/ConfigAdminWeb",
                                ],
                                [
                                        name: "RemoteServicesAdmin",
                                        url:  "https://localhost:8080/aps/RemoteSvcAdmin"
                                ]
                        ]
                ]
        ] as Map<String, Object>)
    }

    @Test
    void testBadHeader() throws Exception {

        try {
            verifier.validate ( [
                    header: [
                            type      : "service",
                            address   : "aps.admin.web",
                            classifier: "publicc"
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
            ] as Map<String, Object>)

            throw new Exception("This document is not valid! header.classifier: publicc is wrong!")
        }
        catch ( IllegalStateException e ) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testMissingUrl() throws Exception {

        try {
            verifier.validate ( [
                    header: [
                            type      : "service",
                            address   : "aps.admin.web",
                            classifier: "public"
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
                                    ]
                            ]
                    ]
            ] as Map<String, Object>)

            throw new Exception ( "This document is not valid! webs[1] is missing url!" )
        }
        catch ( IllegalStateException e ) {
            println "Correctly cauth exception: ${e.message}"
        }
    }


    @Test
    void testToManyFields() throws Exception {

        try {
            verifier.validate ( [
                    header: [
                            type      : "service",
                            address   : "aps.admin.web",
                            classifier: "public"
                    ],
                    body  : [
                            action: "get-webs"
                    ],
                    reply : [
                            webs: [
                                    [
                                            name: "ConfigAdmin",
                                            url : "http://localhost:8080/aps/ConfigAdminWeb",
                                            active: "false"
                                    ],
                                    [
                                            name: "RemoteServicesAdmin",
                                            url : "https://localhost:8080/aps/RemoteSvcAdmin"
                                    ]
                            ]
                    ]
            ] as Map<String, Object>)

            throw new Exception ( "This document is not valid! reply.webs.active is invalid!" )
        }
        catch ( IllegalStateException e ) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testNoReply() throws Exception {

        verifier.validate([
                header: [
                        type:       "service",
                        address:    "aps.admin.web",
                        classifier: "public"
                ],
                body: [
                        action: "get-webs"
                ]
        ] as Map<String, Object>)
    }

    @Test
    void testNoBody() throws Exception {

        try {
            verifier.validate([
                    header: [
                            type      : "service",
                            address   : "aps.admin.web",
                            classifier: "public"
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
            ] as Map<String, Object>)
        }
        catch ( IllegalStateException e ) {
            println "Correctly cauth exception: ${e}"
        }
    }

    @Test
    void testBadAddress() throws Exception {

        try {
            verifier.validate([
                    header: [
                            type      : "service",
                            address   : "aps.adminweb",
                            classifier: "public"
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
            ] as Map<String, Object>)
        }
        catch ( IllegalStateException e ) {
            println "Correctly cauth exception: ${e}"
        }
    }

}
