Installation on Linux
=====================

Prerequisites
-------------

QWAZR Search requires a JAVA 8 SDK.

On a yum based Linux distribution (Centos, Fedora, Redhat):

```bash
yum install java-1.8.0-openjdk-headless
```

On a Debian based Linux distribution (Debian, Ubuntu):

```bash
apt-get install openjdk-8-jdk-headless
```

Download the binary
-------------------

[qwazr-search-1.4.0-SNAPSHOT-exec.jar](http://download.opensearchserver.com/qwazr-search/qwazr-search-1.4.0-SNAPSHOT-exec.jar)


Start the daemon
----------------

```bash
java -jar qwazr-search-1.4.0-SNAPSHOT-exec.jar
```

Two directories are created:

- index: this directory contains the schema and the indexes.
- tmp: this directory contains temporary files.

Test the web service
--------------------

```bash
curl http://localhost:9091/
```

To know more bout the Web service, have a look at the [JSON Web service overview](webservice.md)


Installation as a daemon
------------------------

JAVA daemon are really easy to setup with [systemd](https://en.wikipedia.org/wiki/Systemd).

Here is an example:
- We suppose you have create a "qwazr" user,
- A working directory is located here: /var/lib/qwarz/search

### User creation

```bash
useradd -g daemon -m -b /var/lib/qwazr qwazr
su - qwazr
mkdir /var/lib/qwazr/search
```

### Download the binary:

```bash
cd /var/lib/qwazr/search
curl -O "http://download.opensearchserver.com/qwazr-search/qwazr-search-1.4.0-SNAPSHOT-exec.jar"
```

### Systemd init script

Here is a working systemd init script.

You may use the name **qwazr-search.service**.

```
[Unit]
Description=QWAZR Search

[Service]
User=qwazr
Group=daemon
WorkingDirectory=/var/lib/qwazr/search
ExecStart=/usr/bin/java -XX:+UseG1GC -Djava.net.preferIPv4Stack=true qwazr-search-1.4.0-SNAPSHOT-exec.jar
Environment=LISTEN_ADDR=127.0.0.1
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=15

[Install]
WantedBy=multi-user.target
```

Move this script in the appropriate directory in your Linux distribution.
Use the systemctl commands.

```bash
systemctl start qwazr-search
systemctl stop qwazr-search
```