# Check an index

This API checks an index:

- **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/check
- **HTTP method**: POST

Parameters:

- **schema_name**: the name of the schema
- **index_name**: the name of the index

```shell
curl -XPOST "http://localhost:9091/indexes/my_schema/my_index/check"
```

## Response

Returns a JSON object with the result of the check.

```json
{
  "...":"..."
}
```
