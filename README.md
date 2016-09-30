QWAZR SEARCH
============

A Search Engine microservice with indexing and search features based on [Lucene](https://lucene.apache.org/core/).

Run using Docker
----------------

    docker run qwazr/search
    

### Create a schema

    curl -XPOST  "http://localhost:9091/indexes/my_schema"

### Create an index

     curl -XPOST  "http://localhost:9091/indexes/my_schema/my_index"
  
It returns:

```json
{
  "num_docs": 0,
  "num_deleted_docs": 0,
  "settings": {}
}
```

### Define some fields

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

Create the fields by posting the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_fields.json \
        "http://localhost:9091/indexes/my_uuid/my_index/fields"


It is basically a set of REST/JSON API which are described here:

- [API documentation](src/doc/api)