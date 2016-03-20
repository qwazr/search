# Create an index

This API create a new index:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}
* **HTTP method**: POST

If the index already exists, nothing is changed.

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```
curl -XPOST "http://localhost:9091/indexes/my_schema/my_index"
```

It is also possible to set the following settings:

```shell
curl -POST -H 'Content-Type: application/json' \
    "http://localhost:9091/indexes/my_schema" -d @my_payload
```

Where the payload (my_payload) can contains:

```json
{
    "similarity_class": "org.apache.lucene.search.similarities.DefaultSimilarity"
}
```

## Settings parameters

* **similarity_class**: The name of the Java class used for the similarity.

Possible values are:

* org.apache.lucene.search.similarities.BM25Similarity
* org.apache.lucene.search.similarities.DefaultSimilarity
