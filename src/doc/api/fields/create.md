# Create an index

Call this API to create or update an index and set the fields settings:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: A JSON structure describing the fields

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -XPOST -H 'Content-Type: application/json' \
    "http://localhost:9091/indexes/my_schema/my_index -d"
```

Here is
```json
{
  "name": {
    "analyzer": "LikeAnalyzer",
    "stored": true,
    "tokenized": true,
    "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
  },
  "category": {
    "template": "SortedSetMultiDocValuesFacetField"
  },
  "price": {
    "template": "DoubleDocValuesField"
  },
  "size": {
    "template": "LongField"
  },
  "stock": {
    "template": "IntDocValuesField"
  }
}'
```

## Response

The API returns the settings.

```json
{
  "num_docs" : 0,
  "num_deleted_docs" : 0,
  "fields" : {
    "name" : {
      "analyzer" : "LikeAnalyzer",
      "tokenized" : true,
      "stored" : true,
      "index_options" : "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
    },
    "category" : {
      "template" : "SortedSetMultiDocValuesFacetField"
    },
    "price" : {
      "template" : "DoubleDocValuesField"
    },
    "size" : {
      "template" : "LongField"
    },
    "stock" : {
      "template" : "IntDocValuesField"
    }
  }
}
```