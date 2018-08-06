# List all indexes

This API returns a JSON object with all existing indexes and their UUID:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema

```shell
curl -XGET "http://localhost:9091/indexes/my_schema"
```

## Response

```json
{
  "my_index1": "40291f1c-3e5c-11e8-b96d-d43d7ef8f6f2",
  "my_index2": "a7e8b2f5-2268-11e8-8acc-d43D7ef8f6f2"
}
```

If no index exists, an empty JSON object is returned:

```json
{}
```
