# Delete a field

This API delete a field.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/fields/{field_name}
* **HTTP method**: DELETE

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **field_name**: the name of the field

```shell
curl -XDELETE "http://localhost:9091/indexes/my_schema/my_index/field/my_field"
```

## Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Field not found: my_field"
}
```