# Delete an analyzer

This API delete an analyzer.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: DELETE

Parameters:

* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XDELETE "http://localhost:9091/indexes/my_index/analyzers/FrenchAnalyzer"
```

## Response

If any error occurs:

```json
{
  "status_code" : 404,
  "message" : "Analyzer not found: FrenchAnalyzer"
}
```
