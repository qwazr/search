## Create a schema

This API create a new schema:

* **URL pattern**: {server_name}:9091/indexes/{schema_name}
* **HTTP method**: POST

Parameters:

* **schema_name**: the name of the schema

```
curl -XPOST "http://localhost:9091/indexes/my_schema"
```