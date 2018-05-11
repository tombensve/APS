## MapJsonSchemaMeta

This class scans a MapJson schema as defined by MapJsonDocValidator and extracts a list
of MapJsonEntryMeta instances for each value in a MapJson structure living up to the
schema.

From these the following can be resolved:

- The name of a value.
- The type of a value.
- Is the value required ?
- The constraints of the value. If this starts with '?' then the rest is a regular expression.
  If not the value is a constant, that is, the value has to be exactly as the constraint string.
- A description of the value.

This is not used by the MapJsonDocValidator when validating! This is intended for GUI configuration
editors to use to build a configuration GUI producing valid configurations.

Usage:

    Map<String, Object> schema
    ...
    new MapJsonSchemaMeta(schema).mapJsonEntryMetas.each { MapJsonEntryMeta mjem -> ... }


