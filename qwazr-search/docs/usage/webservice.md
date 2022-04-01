JSON Web service overview
=========================

This page provides a general overview of the JSON API.

Create an index
---------------
Let's define the index settings in a file called: **my_index.json**

```json
{
    "primary_key": "id",
    "record_field": "record"
}
```

We can create an index :

    curl -XPOST -H 'Content-Type: application/json' -d @my_index.json \
        "http://localhost:9091/indexes/my_index"
  
Returns:

```json
{
  "num_docs": 0,
  "version": "2",
  "num_deleted_docs": 0
}
```
        
Index documents
---------------
First, we define the documents in a json file: **my_docs.json**

```json
[
  {
    "id": "1",
    "name": "First article",
    "description": "This is the description of the first article.",
    "category": [
      "news",
      "economy"
    ]
  },
  {
    "id": "2",
    "name": "Second article",
    "description": "This is the description of the second article.",
    "category": [
      "news",
      "science"
    ]
  }
]
```

To index the documents just post the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_docs.json \
        "http://localhost:9091/indexes/my_index/json"
        
As a result, you get the number of documents indexed.

```json
{
  "count" : 2
}
```
        
Search request
--------------

Let's define a search query in a json file: **my_search.json**

Here an example of search request:

```json
{
  "start": 0,
  "rows": 10,
  "query": {
      "SimpleQueryParser": {
	"analyzer": "ascii",
      "query_string": "Article",
      "weights": {
        "name": 10,
        "description": 1
     }
   }
  },
  "returned_fields": [
    "*"
  ]
}
```

Execute the search request by posting the json file:

    curl -XPOST -H 'Content-Type: application/json' -d @my_search.json \
        "http://localhost:9091/indexes/my_index/search"
    
And here is the result:

```json
{
  "timer" : {
    "start_time" : "2020-09-08T23:05:24.085+00:00",
    "total_time" : 2
  },
  "documents" : [ {
    "score" : 0.91160774,
    "pos" : 0,
    "fields" : {
      "id" : "1",
      "name" : "First article",
      "description" : "This is the description of the first article.",
      "category" : [ "news", "economy" ]
    }
  }, {
    "score" : 0.91160774,
    "pos" : 1,
    "fields" : {
      "id" : "2",
      "name" : "Second article",
      "description" : "This is the description of the second article.",
      "category" : [ "news", "science" ]
    }
  } ],
  "facets" : { },
  "total_hits" : 2
}
```

Further
-------

Discover [the full API set](../api)
