## Delete a schema

This API delete a schema and all the indexes it contains.

* **URL pattern**: {server_name}:9091/indexes/{schema_name}
* **HTTP method**: DELETE

```shell
curl -XDELETE "localhost:9091/indexes/my_schema"
```

Parameters:

* **schema_name**: the name of the schema

### Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Schema not found: my_schema"
}
```