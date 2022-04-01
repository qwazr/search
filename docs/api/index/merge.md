# Merge two indexes

This API will copy the content of the **merged_index** to the **index_name**.

The merge does not honor any primary key.
If you have two records with the same ID they will both exist after the merge operation.

- **URL pattern**: http://{server_name}:9091/indexes/{index_name}/merge/{merged_index}
- **HTTP method**: POST

Parameters:

- **index_name**: the name of the index where the records will be added to.
- **merged_index**: the name of the index where the records will be copied from.

```shell
curl -XPOST "http://localhost:9091/indexes/my_index/merge/my_other_index"
```

## Response

Returns a JSON object with the index status.

```json
{
  "...":"..."
}
```
