QWAZR SEARCH
============

A Search Engine microservice and JAVA library with indexing and search features based on
[Apache Lucene](https://lucene.apache.org/core/)

[![Documentation](https://img.shields.io/badge/Documentation-orange.svg)](https://www.qwazr.com/search)
[![Build Status](https://jenkins.opensearchserver.com/job/qwazr/job/search/badge/icon)](https://jenkins.opensearchserver.com/job/qwazr/job/search/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-search/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-search)
[![Coverage Status](https://coveralls.io/repos/github/qwazr/search/badge.svg?branch=master)](https://coveralls.io/github/qwazr/search?branch=master)
[![Join the chat at https://gitter.im/qwazr/QWAZR](https://badges.gitter.im/qwazr/QWAZR.svg)](https://gitter.im/qwazr/QWAZR)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


Getting started
---------------

### Get the image from Docker Hub and run it:

     docker pull qwazr/search 
     docker run -p 9091:9091 qwazr/search
     
     
### Create an index:

We set the primary key to the property "id", and the json record will be stored in a property called "record".
 
     curl -XPOST -H "Content-Type: application/json" \
        -d '{"primary_key":"id","record_field":"record"}' \
        http://localhost:9091/indexes/my_index

 
### Add a document to the index:

     curl -XPOST -H "Content-Type: application/json" \
        -d '{"id": 1, "title":"my article", "rating": 5}' \
        http://localhost:9091/indexes/my_index/json

### Search:
 
For instance, we will use a JSON file called "my_search" to store the search query:

```json
{
  "start": 0,
  "rows": 10,
  "query": {
    "SimpleQueryParser": {
	"analyzer": "ascii",
    "query_string": "Article",
    "weights": {
       "title": 1
     }
   }
  },
  "returned_fields": [
    "*"
  ]
}
```

We can run the query by posting it:

     curl -XPOST -H 'Content-Type: application/json' \
        -d @my_search.json \
         http://localhost:9091/indexes/my_index/search


And voila:

```json
{
  "timer" : {
    "start_time" : "2020-09-08T23:30:40.330+00:00",
    "total_time" : 2
  },
  "documents" : [ {
    "score" : 0.13076457,
    "pos" : 0,
    "fields" : {
      "id" : 1,
      "title" : "my article",
      "rating" : 5
    }
  } ],
  "facets" : { },
  "total_hits" : 1
}    
```
