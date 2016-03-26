# Get a document

Use this API to return the stored field of a document.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/doc/{document_id}
* **HTTP method**: GET
* **Content-Type**: application/json

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **document_id**: The primary key of the document

```shell
curl -XGET -d "http://localhost:9091/indexes/my_schema/my_index/doc/5"
```

## Response

The API returns the stored fields of the document:

```json
{
  "price" : 3.3,
  "stock" : 0
}
```