## Setting schema settings

This API displays the settings of the schema:

* **URL pattern**: {server_name}:9091/indexes/{schema_name}
* **HTTP method**: GET

Parameters:

* **schema_name**: the name of the schema

```shell
curl localhost:9091/indexes/my_schema
```

### Response

The API returns the settings.

```json
{
  "max_simultaneous_write" : 2,
  "max_simultaneous_read" : 5,
  "max_size" : 100000
}
```

### Settings parameters

* **max_size**: The maximum number of documents in the schema.
* **max_simultaneous_read**: The maximum number of simultaneous read access.
* **max_simultaneous_write**: The maximum number of simultaneous write access.