## Delete all documents

This API deletes all the documents of a given index.

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/docs
* **HTTP method**: DELETE

Parameters:

* **index_name**: the name of the index

```shell
curl -XDELETE "http://localhost:9091/indexes/my_index/docs"
```
