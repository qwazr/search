## Update the DocValue fields of several documents

Use this API to update the DocValue fields of a collection of document.

* **URL pattern**: {server_name}:9091/indexes/{schema_name}/{index_name}/docs/values
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON array of JSON object describing the DocValue fields and values

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

The field $id$ must be provided to identify the document which will be updated.

```shell
curl -H 'Content-Type: application/json' \
    -XPOST localhost:9091/indexes/my_schema/my_index/docs/values -d '
[
  {
    "$id$": "1",
    "stock": 14
  },
  {
    "$id$": "2",
    "stock": 13
  }
]'
```
