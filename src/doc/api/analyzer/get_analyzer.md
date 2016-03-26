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
  "tokenizer" : {
    "class" : "standard.StandardTokenizer"
  },
  "filters" : [ {
    "class" : "en.FrenchMinimalStemFilter"
  }, {
    "class" : "core.LowerCaseFilter"
  }, {
    "class" : "miscellaneous.ASCIIFoldingFilter",
    "preserveOriginal" : "true"
  } ]
}
```