JSON Web service overview
=========================

This page provides a general overview of the JSON API.

## Create a schema

    curl -XPOST  "http://localhost:9091/indexes/my_schema"

## Create an index

     curl -XPOST  "http://localhost:9091/indexes/my_schema/my_index"
  
It returns:

```json
{
  "num_docs": 0,
  "version": "2",
  "num_deleted_docs": 0,
  "settings": {}
}
```

## Define some fields

Define some fields in a json file: **my_fields.json**
 
Here is the content:

```json
{
  "$id$": {
    "template": "StringField"
  },
  "name": {
    "tokenized": true,
    "analyzer": "en.EnglishAnalyzer",
    "stored": true,
    "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
  },
  "description": {
    "tokenized": true,
    "analyzer": "en.EnglishAnalyzer",
    "stored": true,
    "index_options": "DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS"
  },
  "category": {
    "template": "SortedSetMultiDocValuesFacetField"
  }
}
```

Upload the fields definition by posting the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_fields.json \
        "http://localhost:9091/indexes/my_schema/my_index/fields"
        
## Index documents

Define the documents in a json file: **my_docs.json**

```json
[
  {
    "$id$": "1",
    "name": "First article",
    "description": "This is the description of the first article.",
    "category": [
      "news",
      "economy"
    ]
  },
  {
    "$id$": "2",
    "name": "Second article",
    "description": "This is the description of the second article.",
    "category": [
      "news",
      "science"
    ]
  }
]
```

Index the documents by posting the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_docs.json \
        "http://localhost:9091/indexes/my_schema/my_index/docs"
        
## Search request

Define your search query in a json file: **my_search.json**

Here an example of search request:

```json
{
  "start": 0,
  "rows": 10,
  "query": {
    "query": "BooleanQuery",
    "clauses": [
      {
        "occur": "must",
        "query": {
          "query": "MultiFieldQueryParser",
          "fields": [
            "name",
            "description"
          ],
          "query_string": "Article",
          "boosts": {
            "name": 10,
            "description": 1
          }
        }
      },
      {
        "occur": "filter",
        "query": {
          "query": "BooleanQuery",
          "clauses": [
            {
              "occur": "must",
              "query": {
                "query": "FacetPathQuery",
                "dimension": "category",
                "path": [
                  "news"
                ]
              }
            }
          ]
        }
      }
    ]
  },
  "facets": {
    "category": {
      "top": 10
    }
  },
  "returned_fields": [
    "name"
  ],
  "highlighters": {
    "my_description": {
      "field": "description"
    }
  }
}
```

Execute the search request by posting the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_search.json \
        "http://localhost:9091/indexes/my_schema/my_index/search"
    
And here is the result:

```json
{
  "timer" : {
    "start_time" : "2016-10-01T12:19:25.311+0000",
    "total_time" : 104,
    "unknown_time" : 0,
    "durations" : {
      "search_query" : 59,
      "facet_count" : 5,
      "storedFields" : 19,
      "docValuesFields" : 6,
      "highlighting" : 15
    }
  },
  "total_hits" : 2,
  "max_score" : 1.7962807,
  "documents" : [ {
    "score" : 1.7962807,
    "percent_score" : 1.0,
    "doc" : 0,
    "shard_index" : -1,
    "highlights" : {
      "my_description" : "This is the description of the first <b>article</b>."
    },
    "fields" : {
      "name" : "First article"
    }
  }, {
    "score" : 1.7962807,
    "percent_score" : 1.0,
    "doc" : 1,
    "shard_index" : -1,
    "highlights" : {
      "my_description" : "This is the description of the second <b>article</b>."
    },
    "fields" : {
      "name" : "Second article"
    }
  } ],
  "facets" : {
    "category" : {
      "news" : 2,
      "economy" : 1,
      "science" : 1
    }
  }
}
```

Further
-------

Discover [the full API](../api)