## MapJsonDocValidator

This takes a schema (made up of a ` Map<String, Object> `, see below) and another ` Map<String, Object> ` representing the JSON. So the catch here is that you need a JSON parser that allows you to get the content as a Map. The Vertx JSON parser does. This uses `Map` since it is generic, does not need to hardcode dependency on a specific parser, and maps are very easy to work with in Groovy.

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

In this case `nameAdress` can contain any number of entries as long as each entry have a key containging a-z or 0-9 or \_ or - and there must be at least one character, and the value only contains numbers and dots.

Note that when the key is a regexp (starts with '?') then there can be no more rule for this submap!

### Schema

#### Keys

\<key\>\_0 - The key is optional.

\<key\>\_1 - The key is required.

&lt;key&gt;\_? - A description of the key. MapJsonSchemaMeta extracts this information. It is intended for editors editing data of a file validated by a schema. This should provide help information about the value. Since APS uses the MapJsonDocSchemaValidator for configurations and is intended to have web guis for editing configuration this is intended to provide information about configuration fields.

?regexp - special handling. See usage above.

#### Values

##### "?regexp"

The '?' indicates that the rest of the value is a regular expression. This regular expression will be applied to each value.

##### "<hash><range>"

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


