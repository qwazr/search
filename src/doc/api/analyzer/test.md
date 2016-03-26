# Test an analyzer

Use this API to submit a text to an analyzer:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: POST

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XPOST -H 'Content-Type: text/plain'  -d "Déjà vu" \
    "http://localhost:9091/indexes/my_schema/my_index/analyzers/FrenchAnalyzer"
```



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
