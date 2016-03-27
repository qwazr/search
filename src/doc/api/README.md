# Search APIs

These pages describe the REST/JSON API of the Search module.

## Schema

A schema is a collection of indexes.

* [Create/update a schema](schema/create.md)
* [List all schemas](schema/list.md)
* [Delete a schema](schema/delete.md)

## Index

An index is a collection of documents.

* [List all indexes](index/list.md)
* [Create/update an index and its settings](index/create.md)
* [Getting the status of an index](index/status.md)
* [Delete an index](index/delete.md)

## Analyzers

* [Create/update an analyzer](analyzer/set_analyzer.md)
* [Getting an analyzer](analyzer/get_analyzer.md)
* [Create/update a collection of analyzer](analyzer/set_analyzers.md)
* [Getting a collection of analyzer](analyzer/get_analyzers.md)
* [Test an analyzer](analyzer/test.md)
* [Delete an analyzer](analyzer/delete.md)

## Fields

* [Create/update a field](fields/set_field.md)
* [Getting a field](fields/get_field.md)
* [Create/update of a collection of field](fields/set_fields.md)
* [Getting a collection of field](fields/get_fields.md)
* [Field types](fields/field_types.md)
* [Delete a field](fields/delete.md)

## Document

* [Insert/update a document](document/update_document.md)
* [Insert/update a collection of document](document/update_documents.md)
* [Update DocValue fields of a document](document/update_docvalue.md)
* [Update DocValue fields from a collection of document](document/update_docvalues.md)
* [Get a document](document/get_document.md)
* [Delete all documents](document/truncate_index.md)

## Search

* [How to build a search request](search/build_search_request.md)
* [Search query](search/index_search.md)
* [Distributed search](search/schema_search.md)
* [Delete by query](search/delete_by_query.md)

## Queries

* [Standard query parser](queries/standard_query_parser.md)
* [Multifield query parser](queries/multifield_query_parser.md)