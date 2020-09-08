# Get the analyzers settings

Call this API to display the collection of analyzers:

* **URL pattern**: http://{server_name}:9091/indexes/{index_name}/analyzers
* **HTTP method**: GET

Parameters:

* **index_name**: the name of the index

```shell
curl -XGET "http://localhost:9091/indexes/my_index/analyzers"
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
