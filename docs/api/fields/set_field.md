# Create/update a field

This API create a new field or update an existing one:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/fields/{field_name}
* **HTTP method**: POST

Parameters:

* **index_name**: the name of the index
* **field_name**: the name of the field

```shell
curl -XPOST -H 'Content-Type: application/json'  -d @my_payload \
    "http://localhost:9091/indexes/my_index/fields/my_field"
```

Where the payload file (my_payload) contains the field definition:

```json
{
  "analyzer": "EnglishAnalyzer",
  "stored": true,
  "tokenized": true,
  "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
}
```
