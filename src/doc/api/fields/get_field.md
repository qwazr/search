# Get a field settings

Call this API to display a field:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/fields/{field_name}
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **field_name**: the name of the field

```shell
curl -XGET "http://localhost:9091/indexes/my_schema/my_index/fields/my_field"
```

## Response

The API returns the field settings.

```json
{
  "analyzer": "EnglishAnalyzer",
  "stored": true,
  "tokenized": true,
  "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
}
```