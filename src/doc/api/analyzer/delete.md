# Delete an analyzer

This API delete an analyzer.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: DELETE

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XDELETE "http://localhost:9091/indexes/my_schema/my_index/analyzers/FrenchAnalyzer"
```

## Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Index not found: my_index"
}
```