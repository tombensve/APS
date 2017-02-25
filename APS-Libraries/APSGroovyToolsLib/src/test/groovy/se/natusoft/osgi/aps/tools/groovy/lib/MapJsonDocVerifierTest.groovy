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
                                    url_1: "?^https?://.*",
                                    no1_0: "#1-100",
                                    no2_0: "#<=10",
                                    no3_0: "#>100",
                                    no4_0: "#1.2-3.4"
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
            println "Correctly cauth exception: ${e.message}"
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
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testIntRangeOK() throws Exception {
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
                                        no1:  22
                                ],
                        ]
                ]
        ] as Map<String, Object>)

    }

    @Test
    void testIntRangeNotOK() throws Exception {
        try {
            verifier.validate([
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
                                            no1 : 222
                                    ],
                            ]
                    ]
            ] as Map<String, Object>)
        }
        catch (IllegalStateException e) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testIntLessThanOK() throws Exception {
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
                                        no2:  -4
                                ],
                        ]
                ]
        ] as Map<String, Object>)

    }

    @Test
    void testIntLessThanNotOK() throws Exception {
        try {
            verifier.validate([
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
                                            no2 : 34
                                    ],
                            ]
                    ]
            ] as Map<String, Object>)
        }
        catch (IllegalStateException e) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testIntGreaterThanOK() throws Exception {
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
                                        no3:  150
                                ],
                        ]
                ]
        ] as Map<String, Object>)

    }

    @Test
    void testIntGreaterThanNotOK() throws Exception {
        try {
            verifier.validate([
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
                                            no3 : 42
                                    ],
                            ]
                    ]
            ] as Map<String, Object>)
        }
        catch (IllegalStateException e) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

    @Test
    void testFloatRangeOK() throws Exception {
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
                                        no4:  1.5f
                                ],
                        ]
                ]
        ] as Map<String, Object>)

    }

    @Test
    void testFloatRangeNotOK() throws Exception {
        try {
            verifier.validate([
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
                                            no4 : 5.6f
                                    ],
                            ]
                    ]
            ] as Map<String, Object>)
        }
        catch (IllegalStateException e) {
            println "Correctly cauth exception: ${e.message}"
        }
    }

}
