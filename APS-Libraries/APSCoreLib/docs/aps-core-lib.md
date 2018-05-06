# aps-core-lib

## MapJsonDocValidator

This takes a schema (made up of a `Map<String, Object>`, see below) and another `Map<String, Object>` representing the JSON. So the catch here is that you need a JSON parser that allows you to get the content as a Map.

The APS JSON parser of course does support this. The Vert.x JSON parser seems to do this, it was from that I got
the idea. The Vert.x parser however only return a `Map` at the top level. If you get a value from the `Map` it will not be a `Map`!

### Usage (Groovy example)

             private Map<String, Object> schema = [
                    "meta/header": "meta",
                    header_1: [
                            type_1      : "service",
                            "meta/type" : "metadata",
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

            private MapJsonDocValidator verifier = new MapJsonDocValidator( validStructure: schema )

            ...

            verifier.validate(myJsonMap)

This will throw a runtime exception on validation failure.

### Schema

#### Keys

<key>_0 - The key is optional.

<key>_1 - The key is required.

#### Values

##### "?regexp"

The '?' indicates that the rest of the value is a regular expression. This regular expression will be applied to each value.

##### "|value|value|..." (Enum)

The '|' indicates the rest of the value is an enum in the format `value|value|value`.

So why this, when it really is a valid regular expression ? There are 2 intentions with the schemas.

1) Validate JSON.

2) Provide meta data about the contents for content editors. The '|' makes it easier to figure out that this is just a list of valid values, and makes it clear that this is the only format allowed for '|'.

The MapJsonSchemaMeta class takes a `Map<String, Object>` schema and provides easier to use meta data about content values.

##### "\#\<range\>"

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

         Map<String, Object> struct = [
            header_1: [
               type_1      : "service",
               address_1   : "aps.admin.web",
               classifier_0: "|public|private"
            ],
            body_1  : [
               action_1: "get-webs"
            ],
            reply_0: [
               webs_1: [
                  [
                     name_1: "?.*",
                     url_0: "?^https?://.*",
                     someNumber_0: "#0-100"
                  ]
               ]
            ]
         ]

## MapJsonSchemaMeta

This class scans a MapJson schema as defined by MapJsonDocValidator and extracts a list of MapJsonEntryMeta instances for each value in a MapJson structure living up to the schema.

From these the following can be resolved:

*  The name of a value.

*  The type of a value.

*  Is the value required ?

*  The constraints of the value. If this starts with '?' then the rest is a regular expression.  If not the value is a constant, that is, the value has to be exactly as the constraint string.

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

Note that this class is also a Map<String, Object>!

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
        )

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

