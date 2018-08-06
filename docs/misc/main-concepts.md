Main concepts
=============

Schema
------

A schema is mainly a directory which contains indexes.
The name of the schema is the name of the directory.
(some file system restrictions must be considered).

It is possible to set a concurrency limit on read and write.

The available parameters are:
* **max_simultaneous_read**: The maximum number of simultaneous read access.
* **max_simultaneous_write**: The maximum number of simultaneous write access.
* **backup_directory_path**: A path to the backup to directory.

The settings are stored in a file named **settings.json** stored in the root of the directory.

[Schema JSON API](/search/api/schema/)

Index
-----

An index is stored in a Schema directory.
The name of the index is the name of the directory
(some file system restrictions must be considered).

Several configuration parameters are available. See the JSON API to see whose are available.

[Index JSON API](/search/api/index/)


Analyzers
---------

Analyzers are a set of rules that will be apply to a text.

They are used at indexing time to build tokens that will be indexed.

The text is first divided into tokens, and on each tokens, a collection of transformations can be applied.

There are different kind of **tokenizer**, as well as different kind of **filters**.

A tokenizer can create a new token when a blank character (space, tabulation) is found.
Words can be divided when case changes, or when digits follow letter.

Filters can remove plural, reject words because they are part of the stop words.

At querying time, the analyzer is used to create the word list which will be use to build the search query.
 
[Analyzer JSON API](/search/api/analyzer/)


Fields
------

Depending on how you want to query the index, for each kind of field, you have to choose a type.
In an index, the typical options for a fields are:
- Enabling full-text indexing
- Shall we sort the result on this field ?
- Will it be used as a filter ?
- Shall we compute counting on this field ?
- Do we want the field to be returned in the response ? With or without highlighting ?

To go further:
- [Field types](/search/api/fields/field_types.md)
- [Fields JSON API](/search/api/fields/)
