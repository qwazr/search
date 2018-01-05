QWAZR Search
============

A **Search server** and an embeddable JAVA library
based on [Apache Lucene](http://lucene.apache.org/core).

It is a **production ready** software.
Already used with **heavy load** environment by several companies.

*It is not a SOLR integration or fork, neither an ElasticSearch one.*

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
- Replication, online backup.

Server installation
-------------------
You can deploy it as a microservice and use its REST/JSON API: 
- Using the [Docker image](usage/docker.md)
- Deployed as a service on a [Linux server](usage/linux.md)
- How to use the [JON Web service](api)

Used as a JAVA library
----------------------
Developed in JAVA, it can be use embedded as a JAVA library in any JAVA 8 project.
Thanks to the JAVA annotations, QWAZR Search provides
an [Object-relational mapping](https://en.wikipedia.org/wiki/Object-relational_mapping)
over Lucene indexes.

Getting started with the [QWAZR Search JAVA library](usage/maven.md)

Open source
-----------
The source code of the project is hosted at
[github/qwazr/search](https://github.com/qwazr/search).

As a QWAZR components it is released under the
[Apache 2 license](https://www.apache.org/licenses/LICENSE-2.0).