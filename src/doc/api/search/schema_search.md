# Distributed search

Call this API to execute a distributed search query.
The search is executed over all indexes of a given schema.

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/*/search
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the query

Parameters:

* **schema_name**: the name of the schema

```shell
curl -XPOST -H 'Content-Type: application/json'  -d @my_payload \
     "http://localhost:9091/indexes/my_schema/*/search"
```

Where the payload file (my_payload) contains the query:

```json
{
  "query_string": "name",
  "default_field": "name",
  "returned_fields": [
    "name",
    "price"
  ],
  "start": 0,
  "rows": 10,
  "facets": {
    "category": { "top": 10 }
  },
  "facet_filters": [
    {
      "category": [
        "cat3",
        "cat5"
      ]
    },
    {
      "category": [
        "cat4"
      ]
    }
  ],
  "sorts": {
    "$score": "descending",
    "price": "ascending"
  }
}
```

### Response

The API returns the documents.

```json
{
  "timer" : {
    "search_query" : 3,
    "facet_count" : 0,
    "returned_fields" : 1,
    "facet_fields" : 0
  },
  "total_hits" : 3,
  "max_score" : "NaN",
  "documents" : [ {
    "score" : "NaN",
    "fields" : {
      "name" : "Second name",
      "price" : 2.2
    }
  }, {
    "score" : "NaN",
    "fields" : {
      "name" : "Third name",
      "price" : 3.3
    }
  }, {
    "score" : "NaN",
    "fields" : {
      "name" : "Fifth name",
      "price" : 10.5
    }
  } ],
  "facets" : {
    "category" : {
      "cat1" : 3,
      "cat2" : 3,
      "cat3" : 1
    }
  }
}
```