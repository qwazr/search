# Get an analyzer settings

Call this API to display an analyzer settings:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XGET "http://localhost:9091/indexes/my_schema/my_index/analyzers/my_french_analyzer"
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