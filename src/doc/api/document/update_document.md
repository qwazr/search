# Insert/update a document

Use this API to insert or update a document into an index.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/doc
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the document

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

The field $id$ is a reserved keyword for the primary key of the document.
If the ID is not provided, a time based UUID is automatically generated.

```shell
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/doc"
```

```json
{
  "$id$": "5",
  "name": "Fifth name",
  "category": [
    "cat1",
    "cat2"
  ],
  "size": 500,
  "price": 10.50,
  "stock": 0
}
```
