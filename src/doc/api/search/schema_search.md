# Distributed search

Call this API to execute a distributed search query.
The search is executed over all indexes of the given schema.

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
  "query": {
    "query": "StandardQueryParser",
    "default_field": "name"
  },
  "returned_fields": [
    "name",
    "price"
  ],
  "start": 0,
  "rows": 10,
  "facets": {
    "category": { "top": 10 }
  },
  "sorts": {
     "$score": "descending",
     "price": "ascending"
  }
}
```

### Response

The API returns the documents:

```json
{
  "timer" : {
    "start_time" : "2016-03-27T09:32:41.466+0000",
    "total_time" : 0,
    "unknown_time" : 0,
    "durations" : {
      "search_query" : 0,
      "facet_count" : 0,
      "returned_fields" : 0,
      "facet_fields" : 0
    }
  },
  "total_hits" : 4,
  "max_score" : 0.48553526,
  "documents" : [ {
    "score" : 0.48553526,
    "percent_score" : 1.0,
    "fields" : {
      "name" : "First name",
      "price" : 1.1
    }
  }, {
    "score" : 0.48553526,
    "percent_score" : 1.0,
    "fields" : {
      "name" : "Second name",
      "price" : 2.2
    }
  }, {
    "score" : 0.48553526,
    "percent_score" : 1.0,
    "fields" : {
      "name" : "Third name",
      "price" : 3.3
    }
  }, {
    "score" : 0.48553526,
    "percent_score" : 1.0,
    "fields" : {
      "name" : "Fourth name",
      "price" : 4.4
    }
  } ],
  "facets" : {
    "category" : {
      "cat1" : 4,
      "cat2" : 3,
      "cat3" : 2,
      "cat4" : 1
    }
  }
}
```