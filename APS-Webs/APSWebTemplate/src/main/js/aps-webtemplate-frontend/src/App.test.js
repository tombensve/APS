import React from 'react'
//import ReactDOM from 'react-dom'
//import App from './App'
import { apsObject } from "./Utils"
import MapJsonDocSchemaValidator from './MapJsonDocSchemaValidator'

// This obviously fails on document being null. This test was auto generated, and I have
// no idea where document is supposed to come from.
//it( 'renders without crashing', () => {
//    const div = document.createElement( 'div' );
//    ReactDOM.render( <App/>, div );
//    ReactDOM.unmountComponentAtNode( div );
//} );

it( 'shows a readable object', () => {
    let myObj = apsObject( { what: "qaz", ever: "wsx" } );
    console.log( myObj.display() );
    if ( myObj.display() !== "{\"what\":\"qaz\",\"ever\":\"wsx\"}" ) throw new Error( "apsObjects .display did not match expected value1" );
} );

it( 'MapJsonDocSchemaValidator works', () => {

    // Do not expect this test data to make any kind of sense in content :-). Its the structure and the _0, _1, etc
    // that is important.

    let schema = {
        "header_?": "Description",
        "header_1": {
            "type_1": "service",
            "address_1": "?aps\\.admin\\..*",
            "classifier_1": "?^public$|^private$", // Always use this for enum values.
            "enabled_0": "!"
        },
        "body_1": {
            "action_1": "get-webs"
        },
        "reply_0": {
            "webs_1": [
                {
                    "name_1": "?.*",
                    "url_1": "?^https?://.*",
                    "no1_0": "#1-100",
                    "no2_0": "#<=10",
                    "no3_0": "#>100",
                    "no4_0": "#1.2-3.4"
                }
            ]
        }
    };

    let verifier = new MapJsonDocSchemaValidator( schema );

    // Validate OK
    console.log( "========== Validating OK! ==========" );
    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public",
            "enabled": true
        },
        "body": {
            "action": "get-webs"
        },
        "reply": {
            "webs": [
                {
                    "name": "ConfigAdmin",
                    "url": "http://localhost:8080/aps/ConfigAdminWeb"
                },
                {
                    "name": "RemoteServicesAdmin",
                    "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                }
            ]
        }
    } );

    // Should fail: enabled must be true or false.
    try {
        console.log( "========== Validating failure! ==========" );
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public",
                "enabled": "kalle"
            },
            "body": {
                action: "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb"
                    },
                    {
                        "name": "RemoteServicesAdmin",
                        "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> This document is not valid! header.enabled: \"false\" is wrong!" );
    }
    catch ( e ) {
        //assert e.message.contains( "boolean" );
        if ( e.message.indexOf( "header.enabled: \"false\" is wrong" ) >= 0 ) throw new Error( e.message );

        console.log( `Correctly caught exception: ${ e }` );
    }

    // Bad classifier.
    try {
        console.log( "========== Validating bad classifier ==========" );
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "publicc"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    },
                    {
                        "name": "RemoteServicesAdmin",
                        "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( "This document is not valid! header.classifier: publicc is wrong!" )
    }
    catch ( e ) {
        if ( e.message === "This document is not valid! header.classifier: publicc is wrong!" ) {
            throw new Error( e.message );
        }

        console.log( `Correctly caught exception: ${ e }` );
    }

    // Missing URL
    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                action: "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    },
                    {
                        "name": "RemoteServicesAdmin",
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> This document is not valid! webs[1] is missing url!" );
    }
    catch ( e ) {
        if ( e.message === ">>> This document is not valid! webs[1] is missing url!" ) throw new Error( e.message );

        console.log( `Correctly caught exception: ${ e }` );
    }

    // Non allowed element.
    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                action: "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                        "active": "false"
                    },
                    {
                        "name": "RemoteServicesAdmin",
                        "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> This document is not valid! reply.webs.active is invalid!" )
    }
    catch ( e ) {
        if ( e.message === ">>> This document is not valid! reply.webs.active is invalid!" ) throw new Error( e.message );

        console.log( `Correctly caught exception: ${ e.message }` );
    }

    // no reply
    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public"
        },
        "body": {
            "action": "get-webs"
        }
    } );

    // No body
    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    },
                    {
                        "name": "RemoteServicesAdmin",
                        "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> Missing body is not valid!" );
    }
    catch ( e ) {
        if ( e.message === ">>> Missing body is not valid!" ) throw new Error( e.message );

        console.log( `Correctly caught exception: ${ e.message }` );
    }

    // Bad address

    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.adminweb",
                "classifier": "public"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    },
                    {
                        "name": "RemoteServicesAdmin",
                        "url": "https://localhost:8080/aps/RemoteSvcAdmin"
                    }
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> bad address should have been caught!" );
    }
    catch ( e ) {
        if ( e.message === ">>> bad address should have been caught!" ) throw new Error( e.message );

        console.log( `Correctly cauth exception: ${ e.message }` );
    }

    // Int range OK
    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public"
        },
        "body": {
            "action": "get-webs"
        },
        "reply": {
            "webs": [
                {
                    "name": "ConfigAdmin",
                    "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    "no1": 22
                },
            ]
        }
    } );

    // Int range not OK
    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                        "no1": 222
                    },
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> no1 is out of range!" );
    }
    catch ( e ) {
        if ( e.message === ">>> no1 is out of range!" ) throw new Error( e.message );

        console.log( `Correctly caught exception: ${ e.message }` );
    }

    // Int "less than" OK
    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public"
        },
        "body": {
            "action": "get-webs"
        },
        "reply": {
            "webs": [
                {
                    "name": "ConfigAdmin",
                    "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    "no2": -4
                },
            ]
        }
    } );

    // Int "less than" not OK
    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                        "no2": 34
                    },
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> 34 is not less than 10!" );
    }
    catch ( e ) {
        if ( e.message === ">>> 34 is not less than 10!" ) throw new Error( e.message );
        console.log( `Correctly cauth exception: ${ e.message }` );
    }

    // Int "greater than" OK

    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public"
        },
        "body": {
            "action": "get-webs"
        },
        "reply": {
            "webs": [
                {
                    "name": "ConfigAdmin",
                    "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    "no3": 150
                },
            ]
        }
    } );

    // Int "greater than" not OK

    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                        "no3": 42
                    },
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> 42 is not > 100!" );
    }
    catch ( e ) {
        if ( e.message === ">>> 42 is not > 100!" ) throw new Error( e.message );

        console.log( `Correctly cauth exception: ${ e.message }` );
    }

    // Float range OK
    verifier.validate( {
        "header": {
            "type": "service",
            "address": "aps.admin.web",
            "classifier": "public"
        },
        "body": {
            "action": "get-webs"
        },
        "reply": {
            "webs": [
                {
                    "name": "ConfigAdmin",
                    "url": "http://localhost:8080/aps/ConfigAdminWeb",
                    "no4": 1.5
                },
            ]
        }
    } );

    // Float range not OK

    try {
        verifier.validate( {
            "header": {
                "type": "service",
                "address": "aps.admin.web",
                "classifier": "public"
            },
            "body": {
                "action": "get-webs"
            },
            "reply": {
                "webs": [
                    {
                        "name": "ConfigAdmin",
                        "url": "http://localhost:8080/aps/ConfigAdminWeb",
                        "no4": 5.6
                    },
                ]
            }
        } );

        // noinspection ExceptionCaughtLocallyJS
        throw new Error( ">>> 5.6f is not between 1.2 and 3.4!" );
    }
    catch ( e ) {
        if ( e.message === ">>> 5.6f is not between 1.2 and 3.4!" ) throw new Error( e.message );

        console.log( `Correctly cauth exception: ${ e.message }` );
    }

} );