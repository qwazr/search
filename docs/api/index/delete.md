# Delete an index

This API delete an index and all the documents it contains.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}
* **HTTP method**: DELETE

Parameters:

* **index_name**: the name of the index

```shell
curl -XDELETE "http://localhost:9091/indexes/my_index"
```

## Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Index not found: my_index"
}
```
