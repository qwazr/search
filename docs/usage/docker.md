QWAZR Search with Docker
========================
A simple way to start QWAZR Search as a microservice is to use the provided Docker image.

Check first that you have Docker installed :
[Docker installation](https://docs.docker.com/engine/installation/)

Start the service
-----------------

To start the microservice, just use the typical launch command : 

    docker run -p 9091:9091 qwazr/search

QWAZR components use by default the port 9091. You may use any other port by modifying the -p parameter.

You can check that the server is running by requesting the JSON Web service:

   curl http://localhost:9091

You will get this invite:

```json
{
  "implementation" : {
    "version" : "1.4.0-SNAPSHOT-50416add6f"
  },
  "specification" : {
    "version" : "1.4.0-SNAPSHOT-50416add6f"
  },
  "webservice_endpoints" : [ "/", "/cluster", "/indexes", "/*" ],
  "memory" : {
    "free" : {
      "bytes" : 35369952,
      "text" : "33 MB"
    },
    "total" : {
      "bytes" : 59244544,
      "text" : "56 MB"
    },
    "max" : {
      "bytes" : 466092032,
      "text" : "444 MB"
    },
    "usage" : {
      "bytes" : 24328432,
      "text" : "23 MB"
    }
  },
  "file_stores" : {
    "/" : {
      "type" : "overlay",
      "free" : {
        "bytes" : 61550673920,
        "text" : "57 GB"
      },
      "total" : {
        "bytes" : 67371577344,
        "text" : "62 GB"
      },
      "usage" : 3.515139,
      "used" : {
        "bytes" : 2368204800,
        "text" : "2 GB"
      }
    }
  },
  "runtime" : {
    "activeThreads" : 7,
    "openFiles" : 31
  }
}
```

The run command from Docker is described here:
https://docs.docker.com/engine/reference/commandline/run/

Use an external volume
----------------------
By default the data are persisted into the container.
The preferred mechanism is to use an external disk storage.

The Docker image exposes the following volume: /var/lib/qwazr

You may create a dedicated volume to store your indexes:

    docker volume create my-indexes
    
Then you can run the service this way:   

    docker run --mount source=my-indexes,target=/var/lib/qwazr -p 9091:9091 qwazr/search

See the Docker documentation for more information on how to manage
[Docker volumes](https://docs.docker.com/engine/admin/volumes/volumes/).

Going further
-------------
Now that the Search microservice is running,
let's learn how to use the [JSON Web service](webservice.md)