# Update the DocValue fields of a document

Use this API to update the DocValue fields of a document.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/doc/values
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the DocValue fields and values

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

The field **$id$** must be provided to identify the document which will be updated.

```shell
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/doc/values"
```

Where the payload file (my_payload) contains the fields to update (only DocValues):

```json
{
  "document": 
    {
      "$id$": "5",
      "stock": 10
    },
    "commit_user_data": {
      "my_key" : "my_value"
    }
}
```
