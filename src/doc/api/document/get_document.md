# Get a document

Use this API to return the stored field of a document.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/docs/{document_id}
* **HTTP method**: GET
* **Content-Type**: application/json

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **document_id**: The primary key of the document

```shell
curl -XGET -d "http://localhost:9091/indexes/my_schema/my_index/docs/5"
```

## Response

