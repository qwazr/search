# Create/update an analyzer

This API create a new analyzer or update an existing one:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: POST

Parameters:

* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XPOST -H 'Content-Type: application/json'  -d @my_payload \
    "http://localhost:9091/indexes/my_index/analyzers/FrenchAnalyzer"
```

Where the payload file (my_payload) contains the analyzer definition:

```json
{
  "tokenizer": {
    "class": "standard.StandardTokenizer"
  },
  "filters": [
    {
      "class": "fr.FrenchMinimalStemFilter"
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
