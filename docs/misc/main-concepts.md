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

Index
-----

An index is stored in a Schema directory.
The name of the index is the name of the directory
(some file system restrictions must be considered).

The available settings are:
_to be documented_

Analyzers
---------

...

Fields
------

...

Replication
-----------

...

Backups
-------

...
