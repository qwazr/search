# Update the DocValue fields of several documents

Use this API to update the DocValue fields from a collection of document.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/docs/values
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON array of JSON object describing the DocValue fields and values

Parameters:

* **index_name**: the name of the index

The field $id$ must be provided to identify the document which will be updated.

```shell
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
     "http://localhost:9091/indexes/my_index/docs/values"
```

Where the payload file (my_payload) contains the documents to update (only DocValues):

```json
{
"documents":
    [
      {
        "$id$": "1",
        "stock": 14
      },
      {
        "$id$": "2",
        "stock": 13
      }
    ],
    "commit_user_data": {
      "my_key" : "my_value"
    }
}
```
