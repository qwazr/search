# Test an analyzer

Use this API to submit a text to an analyzer:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}/{index_name}/analyzers/{analyzer_name}
* **HTTP method**: POST

Parameters:

* **schema_name**: the name of the schema
* **index_name**: the name of the index
* **analyzer_name**: the name of the analyzer

```shell
curl -XPOST -H 'Content-Type: text/plain'  -d "Déjà vu" \
    "http://localhost:9091/indexes/my_schema/my_index/analyzers/FrenchAnalyzer"
```

## Response

Returns the extracted terms with the metadata:

```json
[ {
  "char_term" : "deja",
  "start_offset" : 0,
  "end_offset" : 4,
  "position_increment" : 1,
  "position_length" : 1,
  "type" : "<ALPHANUM>",
  "is_keyword" : false
}, {
  "char_term" : "vu",
  "start_offset" : 5,
  "end_offset" : 7,
  "position_increment" : 1,
  "position_length" : 1,
  "type" : "<ALPHANUM>",
  "is_keyword" : false
} ]
```