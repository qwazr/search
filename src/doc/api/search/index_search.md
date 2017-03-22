# Search query

Call this API to execute a search query and get documents:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/search
* **HTTP method**: POST
* **Content-Type**: application/json
* **Body**: a JSON object describing the query

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index

```bash
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/search"
```

Where the payload file (my_payload) contains the search request:

```json
{
  "query": {
    "query": "StandardQueryParser",
    "default_field": "name",
    "query_string": "name"
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
    "start_time" : "2016-03-27T09:27:54.437+0000",
    "total_time" : 3,
    "unknown_time" : 0,
    "durations" : {
      "search_query" : 1,
      "facet_count" : 1,
      "returned_fields" : 1,
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
```