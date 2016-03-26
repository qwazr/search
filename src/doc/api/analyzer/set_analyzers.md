# Create/update all analyzers

This API create a set of analyzers and replace any previous one:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers
* **HTTP method**: POST

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```shell
curl -XPOST -H 'Content-Type: application/json'  -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/analyzers/my_french_analyzer"
```

Where the payload file (my_payload) contains the analyzer definitions:

```json
{
  "LikeAnalyzer": {
    "tokenizer": {
      "class": "standard.StandardTokenizer"
    },
    "filters": [
      {
        "class": "core.LowerCaseFilter"
      },
      {
        "class": "miscellaneous.ASCIIFoldingFilter",
        "preserveOriginal": true
      }
    ]
  },
  "EnglishAnalyzer": {
    "tokenizer": {
      "class": "standard.StandardTokenizer"
    },
    "filters": [
      {
        "class": "en.EnglishPossessiveFilter"
      },
      {
        "class": "en.EnglishMinimalStemFilter"
      },
      {
        "class": "core.LowerCaseFilter"
      }
    ]
  },
  "FrenchAnalyzer": {
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
}
```
