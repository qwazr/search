# Create a schema

This API create a new schema:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}
* **HTTP method**: POST

If the schema already exists, nothing is changed.

Parameters:

* **schema_name**: the name of the schema

```
curl -XPOST "http://localhost:9091/indexes/my_schema"
```

It is also possible to set the following settings:

```shell
curl -POST -H 'Content-Type: application/json' \
    "http://localhost:9091/indexes/my_schema" -d '
{
    "max_size": 100000,
    "max_simultaneous_read": 5,
    "max_simultaneous_write": 2
}'
```

## Settings parameters

* **max_size**: The maximum number of documents in the schema.
* **max_simultaneous_read**: The maximum number of simultaneous read access.
* **max_simultaneous_write**: The maximum number of simultaneous write access.