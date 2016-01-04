## Delete by query

Call this API to execute a search query and delete the document:

* **URL pattern**: {server_name}:9091/indexes/{schema_name}/{index_name}/search?delete=true
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the query

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -H 'Content-Type: application/json' \
    -XPOST localhost:9091/indexes/my_schema/my_index/search?delete=true -d '
{
  "query": {
    "query": "FacetPathQuery",
    "dimension": "category",
    "path": [
      "cat4"
    ]
  }
}'
```

### Response

The API returns the number of deleted documents.

```json
{
  "total_hits" : 3
}
```