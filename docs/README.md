Description
-----------
QWAZR Search is a Search Engine server and/or library
based on [Apache Lucene](http://lucene.apache.org/core).

It is a production ready software.
Already used in production by several companies on heavy load environment.

It is not a SOLR integration or fork, neither an ElasticSearch one.

The following features are provided :

Features
--------
Our goal is to expose the best feature of Lucene. By best, we mean stable and production ready.
It is common, for Lucene, to provide several way to achieve the same goal. We try to expose the best practice.

- Multilingual Full-text indexing,
- Fuzzy search,
- Lemmatisation, phonetic,
- Multi-word synonym,
- Faceting search,
- Updatable fields,
- Replication

Server installation
-------------------
You can deploy it as a microservice and use its REST/JSON API: 
- [Using the DOCKER image](usage/docker.md)
- [Deployed on a LINUX server](usage/linux.md)
- [REST/JSON API](api)

Used as a JAVA library
----------------------
Developed in JAVA, it can be use embedded as a JAVA library in any JAVA 8 project.
Thanks to the JAVA annotations, QWAZR Search provides
an [Object-relational mapping](https://en.wikipedia.org/wiki/Object-relational_mapping)
over Lucene index.

Getting started with the [QWAZR Search JAVA library](usage/maven.md)

License
-------
As a QWAZR components it is released under the
[Apache 2 license](https://www.apache.org/licenses/LICENSE-2.0).