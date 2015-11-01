## Delete an index

This API delete an index and all the documents it contains.

* **URL pattern**: {server_name}:9091/indexes/{schema_name}/{index_name}
* **HTTP method**: DELETE

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -XDELETE "localhost:9091/indexes/my_schema/my_index"
```

### Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Index not found: my_index"
}
```