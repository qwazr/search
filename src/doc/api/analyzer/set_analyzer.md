# Create/update an analyzer

This API create a new analyzer or update an existing one:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: POST

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -POST -H 'Content-Type: application/json'  -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/analyzers/my_french_analyzer"
```

Where the payload file (my_payload) contains the analyzer definition:

```json
{
  "tokenizer": {
    "class": "standard.StandardTokenizer"
  },
  "filters": [
    {
      "class": "en.FrenchMinimalStemFilter"
    },
    {
      "class": "core.LowerCaseFilter"
    },
    {
      "class": "miscellaneous.ASCIIFoldingFilter",
      "preserveOriginal": true
    }
  ]
}
```

### Response

```json
```
