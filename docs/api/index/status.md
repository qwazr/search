# Getting the status of an index

Call this API to display the status of an index:

- **URL pattern**: http://{server_name}:9091/indexes/{index_name}
- **HTTP method**: GET

Parameters:

- **index_name**: the name of the index


```shell
curl -XGET "http://localhost:9091/indexes/my_index"
```

## Response

The API returns the settings.

```json
{
  "num_docs" : 0,
  "num_deleted_docs" : 0,
  "settings" : { }
}
```
