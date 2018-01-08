Installation on Linux
=====================

### Prerequisites

QWAZR Search requires a JAVA 8 SDK.


### Download the binary

[qwazr-search-1.4.0-SNAPSHOT-exec.jar](http://jenkins.opensearchserver.com/view/Docker/job/qwazr-search-docker/com.qwazr$qwazr-search/lastSuccessfulBuild/artifact/com.qwazr/qwazr-search/1.4.0-SNAPSHOT/qwazr-search-1.4.0-SNAPSHOT-exec.jar)

```bash
```

### Start the daemon

```bash
java -jar qwazr-search-1.4.0-SNAPSHOT-exec.jar
```

### Test the web service
```bash
wget http://localhost:9091/
```

To know more bout the Web service, have a look at the [JSON Web service overview](webservice.md)


### Installation as a daemon

JAVA daemon are really easy to setup with [systemd](https://en.wikipedia.org/wiki/Systemd).

Here is an example:
- We suppose you have create a "qwazr" user,
- A working directory is located here: /var/lib/qwarz/search

```bash
useradd -g daemon -m -b /var/lib/qwazr qwazr
su - qwazr
mkdir /var/lib/qwazr/search
```

Download the binary:

```bash
cd /var/lib/qwazr/search
wget http://jenkins.opensearchserver.com/view/Docker/job/qwazr-search-docker/com.qwazr$qwazr-search/lastSuccessfulBuild/artifact/com.qwazr/qwazr-search/1.4.0-SNAPSHOT/qwazr-search-1.4.0-SNAPSHOT-exec.jar
```

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

_Documentation in progress..._