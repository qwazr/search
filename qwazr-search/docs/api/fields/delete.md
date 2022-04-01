# Delete a field

This API delete a field.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/fields/{field_name}
* **HTTP method**: DELETE

Parameters:

* **index_name**: the name of the index
* **field_name**: the name of the field

```shell
curl -XDELETE "http://localhost:9091/indexes/my_index/fields/my_field"
```

## Response

If the field does not exist:

```json
{
  "status_code" : 404,
  "message" : "Field not found: my_field"
}
```
