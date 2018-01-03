# List all indexes

This API returns a list with all existing indexes:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema

```shell
curl -XGET "http://localhost:9091/indexes/my_schema"
```

## Response

```json
[my_index]
```

If no index exists, an empty array is returned:

```json
[]
```