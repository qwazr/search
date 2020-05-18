# List all schemas

This API returns a list with all existing schemas:

* **URL pattern**: http://{server_name}:9091/indexes
* **HTTP method**: GET

```shell
curl -XGET "http://localhost:9091/indexes"
```

## Response

```json
["my_schema"]
```

If no schema exists, an empty array is returned:

```json
[]
```
