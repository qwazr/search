# Get the fields settings

Call this API to display the collection of fields:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/fields
* **HTTP method**: GET

Parameters:

* **index_name**: the name of the index

```shell
curl -XGET "http://localhost:9091/indexes/my_index/fields"
```

## Response

The API returns the settings.

```json
{
  "name": {
    "template": "TextField"
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
