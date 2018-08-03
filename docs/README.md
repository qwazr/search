QWAZR Search
============

[![Build Status](https://travis-ci.org/qwazr/search.svg?branch=master)](https://travis-ci.org/qwazr/search)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-search/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.qwazr/qwazr-search)
[![Coverage Status](https://coveralls.io/repos/github/qwazr/search/badge.svg?branch=master)](https://coveralls.io/github/qwazr/search?branch=master)
[![Join the chat at https://gitter.im/qwazr/QWAZR](https://badges.gitter.im/qwazr/QWAZR.svg)](https://gitter.im/qwazr/QWAZR)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A **Search server** and an embeddable JAVA library
based on [Apache Lucene](http://lucene.apache.org/core).

It is a **production ready** software.
Already used with **heavy load** environment by several companies.

*It is not a SOLR integration or fork, neither an ElasticSearch one.*

Features
--------
Our goal is to expose the best feature of Lucene. By best, we mean stable and production ready.
It is common, for Lucene, to provide several ways to achieve the same goal. We try to expose the best practice.

- Multilingual Full-text indexing,
- Fuzzy search,
- Lemmatisation, phonetic,
- Multi-word synonym,
- Faceting search,
- Updatable fields,
- Replication, online backup.

Use it as a Microservice
------------------------
You can deploy it as a microservice and use the JSON Web service :
- Using the [Docker image](usage/docker.md)
- Deployed as a service on a [Linux server](usage/linux.md)
- How to use the [JSON Web service](usage/webservice.md)

Used as a JAVA library
----------------------
Developed in JAVA, it can be embedded as a JAVA library in any JAVA 8 project.
Thanks to the JAVA annotations, QWAZR Search provides
an [Object-relational mapping](https://en.wikipedia.org/wiki/Object-relational_mapping)
over Lucene indexes.

Getting started with the [QWAZR Search JAVA library](usage/java-library.md)

Open source
-----------
The source code of the project is hosted at
[github/qwazr/search](https://github.com/qwazr/search).

As a QWAZR component it is released under the
[Apache 2 license](https://www.apache.org/licenses/LICENSE-2.0).
