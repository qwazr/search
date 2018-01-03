## Delete all documents

This API deletes all the documents of a given index.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/docs
* **HTTP method**: DELETE

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -XDELETE "http://localhost:9091/indexes/my_schema/my_index/docs"
```