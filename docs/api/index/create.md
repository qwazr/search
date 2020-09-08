# Create an index

This API create a new index:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}
* **HTTP method**: POST

If the index already exists, nothing is changed.

Parameters:

* **index_name**: the name of the index

```
curl -XPOST "http://localhost:9091/indexes/my_index"
```

### Response

```json
{
  "num_docs" : 0,
  "num_deleted_docs" : 0,
  "settings" : { }
}
```

## Create index with settings

It is also possible to set the following settings:

```shell
curl -POST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_index"
```

Where the payload file (my_payload) may contains those settings:

```json
{
    "similarity_class": "org.apache.lucene.search.similarities.ClassicSimilarity",
    "master": {
      "scheme": "http",
      "host": "localhost",
      "port": 9091,
      "path": "/indexes", 
      "timeout": 60000,
      "username": "login",
      "password": "password",
      "schema": "my_schema",
      "index": "my_index"
    },
    "directory_type": "FSDirectory",
    "merge_scheduler": "SERIAL",
    "ram_buffer_size": "512",
    "use_compound_file": true,
    "enable_taxonomy_index": false,
    "index_reader_warmer": "true",
    "merged_segment_warmer": true
}
```

## Settings parameters

- **similarity_class** :
  The name of the Java class used for the similarity.
  The possible values are :
  * org.apache.lucene.search.similarities.BM25Similarity
  * org.apache.lucene.search.similarities.ClassicSimilarity
- **master**: The location of an optional master (for replication)
- **directory_type** : The type of the directory. The possible values are : 
  * FSDirectory
  * RAMDirectory
- **merge_scheduler** :
  The expected behavior for the segment merge. The possible values are : 
  * NO: No merge.
  * CONCURRENT: Concurrent merge.
  * SERIAL: Non concurrent merge.
- **ram_buffer_size** : The size of the RAM buffer (in MB). Higher value may improve the indexing speed.
- **use_compound_file** : Enable or disable the compound file feature.
- **enable_taxonomy_index** : Enable or disable the taxonomy index.
- **index_reader_warmer** : Enable or disable the index reader warmer.
- **merged_segment_warmer** : Enable or disable the merged segment warmer.
