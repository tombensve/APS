# aps-core-lib

## Configuration for Bundles

### APSConfigLoader

This is a trivially easy way of getting configuration. Just do:

        Map<String, Object> config = APSConfigLoader.get("config-id")

Where there are 2 resource files under `apsconfig`:

        apsconfig/
            (config-id)-schema.json
            default-(config-id)-config.json

To provide a configuration that differs from bundle default, package:

        apsconfig/
            (config_id)-config.json

in a jar file and include in APS-Runtime under _dependencies_. This will override the default config file delivered with bundle. It is possible to include multiple bundles config files in the same jar of course.

Do note that if `(config-id)-schema.json` is provided then the configuration file used will be validated against it. If no schema file is provided by the bundle then no validation will be done and whatever is in the config file will be loaded without error. It must of course be a JSON file or it will fail!

## MapJsonDocValidator

This takes a schema (made up of a `Map<String,``Object>`, see below) and another `Map<String,``Object>` representing the JSON. So the catch here is that you need a JSON parser that allows you to get the content as a Map. The Vertx JSON parser does. This uses `Map` since it is generic, does not need to hardcode dependency on a specific parser, and maps are very easy to work with in Groovy.

### Useage

             private Map<String, Object> schema = [
                    "header_?": "Contains meta data about the message.",
                    "header_1": [
                            "type_?"      : "The type of the message. Currently only 'service'.",
                            "type_1"      : "service",
                            "address_?"   : "The address of the sender.",
                            "address_1"   : "?aps\\.admin\\..*",
                            "classifier_1": "?public|private"
                    ],
                    "body_1"  : [
                            "action_1": "get-webs"
                    ],
                    "reply_0": [
                            "webs_1": [
                                    [
                                            "name_1": "?.*",
                                            "url_1": "?^https?://.*",
                                            "no1_0": "#1-100",
                                            "no2_0": "#<=10",
                                            "no3_0": "#>100",
                                            "no4_0": "#1.2-3.4"
                                    ]
                            ]
                    ]
            ] as Map<String, Object>
        
            private MapJsonDocValidator verifier = new MapJsonDocValidator( validStructure: schema )
        
            ...
        
            verifier.validate(myJsonMap)

This will throw a runtime exception on validation failure, specifically APSValidationException.

Note that there is also a special feature for defining a simple dynamic key=>value map where the key is defined with a regexp and the value can be a regexp, string, number, or boolean. This is done be defining a map with only one entry. Example:

        private Map<String, Object> schema = [
            "nameAddress": [
                "?([a-z]|[0-9]|_|-)+": "?[0-9,.]+"
            ]
        ]

In this case `nameAdress` can contain any number of entries as long as each entry have a key containging a-z or 0-9 or _ or - and there must be at least one character, and the value only contains numbers and dots.

Note that when the key is a regexp (starts with '?') then there can be no more rule for this submap!

### Schema

#### Keys

<key>_0 - The key is optional.

<key>_1 - The key is required.

<key>_? - A description of the key. MapJsonSchemaMeta extracts this information. It is intended for editors editing data of a file validated by a schema. This should provide help information about the value. Since APS uses the MapJsonDocSchemaValidator for configurations and is intended to have web guis for editing configuration this is intended to provide information about configuration fields.

?regexp - special handling. See usage above.

#### Values

##### "?regexp"

The '?' indicates that the rest of the value is a regular expression. This regular expression will be applied to each value.

##### "\<hash\>\<range\>"

This indicates that this is a number and defines the number range allowed. The following variants are available:

__"#from-to"__ : This specifies a range of allowed values, from lowest to highest.

__"#<=num"__ : This specifies that the numeric value must be less than or equal to the specified number.

__"#>=num"__ : This specifies that the numeric value must be larger than or equal to the specified number.

__"#<num"__ : This specifies that the numeric value must be less than the specified number.

__"#>num"__ : This specifies that the numeric value must be larger than the specified number.

Note: Both floating point numbers and integers are allowed.

##### "bla"

This requires values to be exactly "bla".

#### Example

        Map<String, Object> myJsonObject = JSON.readJsonAsMap( myJsonStream, jsonErrorHandler)
        
        ...
        
        Map<String, object> schema = JSON.readJsonAsMap(schemaStream, jsonErrorHandler)
        MapJsonDocValidator jsonValidator = new MapJsonDocValidator( validstructure: schema )
        
        jsonValidator.validate( myJsonObject )

## MapJsonSchemaMeta

This class scans a MapJson schema as defined by MapJsonDocValidator and extracts a list of MapJsonEntryMeta instances for each value in a MapJson structure living up to the schema.

From these the following can be resolved:

*  The name of a value.

*  The type of a value.

*  Is the value required ?

*  The constraints of the value. If this starts with '?' then the rest is a regular expression.  If not the value is a constant, that is, the value has to be exactly as the constraint string.

*  A description of the value.

This is not used by the MapJsonDocValidator when validating! This is intended for GUI configuration editors to use to build a configuration GUI producing valid configurations.

Usage:

        Map<String, Object> schema
        ...
        new MapJsonSchemaMeta(schema).mapJsonEntryMetas.each { MapJsonEntryMeta mjem -> ... }

## StructMap

This wraps a structured Map that looks like a JSON document, containing Map, List, and other 'Object's as values.

A key is a String containing branches separated by '.' characters for each sub structure.

It provides a method to collect all value referencing keys in the map structure with full key paths.

It provides a lookup method that takes a full value key path and returns a value.

Note that since this delegates to the wrapped Map the class is also a Map when compiled!

Here is an example (in Groovy) that shows how to lookup and how to use the keys:

        StructMap smap = new StructMap<>(
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
        
        assert smap.lookup( "header.type" ).toString() == "service"
        assert smap.lookup( "header.address" ).toString() == "aps.admin.web"
        assert smap.lookup( "header.classifier" ).toString() == "public"
        assert smap.lookup( "body.action" ).toString() == "get-webs"
        assert smap.lookup( "reply.webs.[0].name") == "ConfigAdmin"
        assert smap.lookup( "reply.webs.[1].name") == "RemoteServicesAdmin"
        assert smap.lookup( "reply.webs.[1].url") == "https://localhost:8080/aps/RemoteSvcAdmin"
        
        smap.withAllKeys { String key ->
            println "${key}"
        }
        
        // will produce:
        header.type
        header.address
        header.classifier
        header.enabled
        body.action
        reply.webs.[2].name
        reply.webs.[2].url

Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map, List, Number (if you stick to JSON formats) or any other type of value you put in there.

Also note the indexes in the keys in the example. It is not "webs[0]" but "webs.[0]"! The index is a reference name in itself. The keys returned by getAllKeys() have a number between the '[' and the ']' for List entries. This number is the number of entries in the list. The StructPath class (used by this class) can be used to provide array size of an array value.

## APSBus

Since there are many types of busses out there and that APS is based on Vert.x with its own bus (_EventBus_) APS provides a very simple, generic bus API called __APSBus__. It should be documented elsewhere in this document.

This bundle contains an implementation of APSBus. The _APSBus_ implementation just tracks all published `APSBusRouter` implementations. Each `APSBusRouter` implementations must also provide a resource file in _aps/bus/routers_ with the name of each bus router, one per line. APSBus will find all these and wait for them to become available as services before publishing itself as a service.

If an `APSBusRouter` implementation does not do this, then it is possible that APSBus will not see it. It is also possible that it will se it, but miss another implementation instead. This due to it actually not knowing the available implementations nor their names. It just count the entries in all found _routers_ files, and waits for that amount of `APSBusRouter` services to be published. The names are just for show and are logged on startup.

