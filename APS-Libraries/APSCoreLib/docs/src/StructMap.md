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

Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map,
List, Number (if you stick to JSON formats) or any other type of value you put in there.

Also note the indexes in the keys in the example. It is not "webs\[0\]" but "webs.\[0\]"! The index is a
reference name in itself. The keys returned by getAllKeys() have a number between the '\[' and the '\]' for
List entries. This number is the number of entries in the list. The StructPath class (used by this class)
can be used to provide array size of an array value.
