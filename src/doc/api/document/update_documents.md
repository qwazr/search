# Insert/update several documents

Use this API to insert or update an array of documents into an index.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/docs
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON array of JSON object describing the documents

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

The field $id$ is a reserved keyword for the primary key of the document.
If the ID is not provided, a time based UUID is automatically generated.

```shell
curl -XPOST -H 'Content-Type: application/json' \
    "http://localhost:9091/indexes/my_schema/my_index/docs" -d '
[
  {
    "$id$": "2",
    "name": "Second name",
    "category": [
      "cat1",
      "cat2"
    ],
    "size": 200,
    "price": 2.20,
    "stock": 0
  },
  {
    "$id$": "3",
    "name": "Third name",
    "category": [
      "cat1",
      "cat2",
      "cat3"
    ],
    "size": 300,
    "price": 3.30,
    "stock": 0
  }
]'
```