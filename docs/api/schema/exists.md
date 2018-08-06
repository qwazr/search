# Check Schema existence

This API checks if a schema exists:

* **URL pattern**: http://{server_name}:9091/indexes/{schema_name}
* **HTTP method**: HEAD

If the schema exists a 200 HTTP code is returned.

If the schema does not exist, a 404 HTTP code is returned.

Parameters:

* **schema_name**: the name of the schema

```
curl -XHEAD "http://localhost:9091/indexes/my_schema"
```
