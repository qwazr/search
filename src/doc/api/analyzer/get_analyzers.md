# Get an analyzer settings

Call this API to display all the analyzers:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -XGET "http://localhost:9091/indexes/my_schema/my_index/analyzers"
```

## Response

The API returns the settings.

```json
{
  "LikeAnalyzer" : {
    "tokenizer" : {
      "class" : "standard.StandardTokenizer"
    },
    "filters" : [ {
      "class" : "core.LowerCaseFilter"
    }, {
      "class" : "miscellaneous.ASCIIFoldingFilter",
      "preserveOriginal" : "true"
    } ]
  },
  "EnglishAnalyzer" : {
    "tokenizer" : {
      "class" : "standard.StandardTokenizer"
    },
    "filters" : [ {
      "class" : "en.EnglishPossessiveFilter"
    }, {
      "class" : "en.EnglishMinimalStemFilter"
    }, {
      "class" : "core.LowerCaseFilter"
    } ]
  },
  "FrenchAnalyzer" : {
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
}
```