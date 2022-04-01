# Insert/update several documents

Use this API to insert or update a collection of documents into an index.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/docs
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON array of JSON object describing the documents

Parameters:

* **index_name**: the name of the index

The field **$id$** is a reserved keyword for the primary key of the document.
If the primary key is not provided, a time based UUID is automatically generated.

```shell
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_index/docs"
```

Where the payload file (my_payload) contains the collection of documents to index:

```json
{
  "documents": [
      {
        "$id$": "1",
        "name": "First name",
        "category": [
          "cat1"
        ],
        "format": "odd",
        "single_date": "20160101",
        "size": 100,
        "price": 1.10,
        "stock": 0
      },
      {
        "$id$": "2",
        "name": "Second name",
        "category": [
          "cat1",
          "cat2"
        ],
        "format": "even",
        "single_date": "20160202",
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
        "format": "odd",
        "single_date": "20160103",
        "size": 300,
        "price": 3.30,
        "stock": 0
      },
      {
        "$id$": "4",
        "name": "Fourth name",
        "category": [
          "cat1",
          "cat2",
          "cat3",
          "cat4"
        ],
        "format": "even",
        "size": 400,
        "price": 4.40,
        "stock": 0
      }
    ],
    "commit_user_data": {
      "my_key" : "my_value"
    }
}
```
