# How to build a search request

Here is the most complex search request.

It includes:

* Full text query
* Filtered query
* Boolean query
* Facet query and facet count
* Snippet and highlighting
* Sorting
* Functions
* Paging
* Returned fields

```json
{
    "query": {
        "type": "BooleanQuery",
        "clauses": [
            {
                "occur": "must",
                "query": {
                    "type": "QueryParser",
                    "default_field": "name",
                    "query_string": "name"
                }
            },
            {
                "occur": "filter",
                "query": {
                    "type": "BooleanQuery",
                    "clauses": [
                        {
                            "occur": "should",
                            "query": {
                                "type": "FacetPathQuery",
                                "dimension": "category",
                                "path": [
                                  "cat3"
                                ]
                            }
                        }
                    ]
                }
            }
        ]
    },
    "returned_fields": [
        "name",
        "price"
    ],
    "start": 0,
    "rows": 10,
    "facets": {
        "category": {},
        "format": {},
        "FacetQueries": {
            "queries": {
                "AllDocs": {
                    "query": "MatchAllDocsQuery"
                },
                "2016,January": {
                    "query": "TermRangeQuery",
                    "field": "single_date",
                    "lower_term": "201601",
                    "upper_term": "201602",
                    "include_lower": true,
                    "include_upper": false
                }
            }
        }
    },
    "functions": [
        {
            "function": "min",
            "field": "price"
        },
        {
            "function": "max",
            "field": "stock"
        }
    ],
    "sorts": {
        "$score": "descending",
        "price": "ascending"
    },
    "highlighters": {
        "my_custom_snippet": {
            "field": "description",
            "pre_tag": "<strong>",
            "post_tag": "</strong>",
            "escape": false,
            "multivalued_separator": " ",
            "ellipsis": "… ",
            "max_passages": 5,
            "max_length": 5000,
            "break_iterator": {
                "type": "sentence",
                "language": "en-US"
            }
        }
    }
}
```

If you already build an index while following the examples on this documentation,
you can test this request using this curl command:

(Where the payload file (my_payload) contains the search request).

```bash
curl -XPOST -H 'Content-Type: application/json' -d @my_payload \
    "http://localhost:9091/indexes/my_schema/my_index/search"
```
