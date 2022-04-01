# Insert/update a document

Use this API to insert or update a document into an index.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/doc
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the document

Parameters:

* **index_name**: the name of the index

The field **$id$** is a reserved keyword for the primary key of the document.
If the primary key is not provided, a time based UUID is automatically generated.

```shell
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_index/doc"
```

Where the payload file (my_payload) contains the document to index:

```json
{
  "document": {
      "$id$": "5",
      "name": "Fifth name",
      "category": [
        "cat1",
        "cat2",
        "cat3",
        "cat4",
        "cat5"
      ],
      "format": "odd",
      "size": 500,
      "price": 10.50,
      "stock": 0,
      "description": [
        "A web search engine is a software system that is designed to search for information on the World Wide Web.",
        "The search results are generally presented in a line of results often referred to as search engine results pages.",
        "The information may be a mix of web pages, images, and other types of files.",
        "Some search engines also mine data available in databases or open directories."
      ]
  },
  "commit_user_data": {
    "my_key": "my_value"
  }
}
```
