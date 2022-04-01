# Create/update the collection of fields

This API create a set of fields and replace any previous one:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/fields
* **HTTP method**: POST

Parameters:

* **index_name**: the name of the index

```shell
curl -XPOST -H 'Content-Type: application/json'  -d @my_payload \
    "http://localhost:9091/indexes/my_index/fields"
```

Where the payload file (my_payload) contains the analyzer definitions:

```json
{
  "name": {
    "template": "TextField",
    "stored": true,
    "analyzer": "EnglishAnalyzer"
  },
  "description": {
    "analyzer": "EnglishAnalyzer",
    "stored": true,
    "tokenized": true,
    "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
  },
  "category": {
    "template": "SortedSetMultiDocValuesFacetField"
  },
  "format": {
    "template": "FacetField"
  },
  "single_date": {
    "template": "StringField"
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
}
```
