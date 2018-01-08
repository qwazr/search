JAVA library integration
========================

Maven integration
-----------------

Include the library in your maven project:

```xml
<dependencies>
    <dependency>
        <groupId>com.qwazr</groupId>
        <artifactId>qwazr-search</artifactId>
        <version>1.4.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

We provide a BOM to help managing libraries conflict:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.qwazr</groupId>
            <artifactId>qwazr-bom</artifactId>
            <version>1.4.0-SNAPSHOT</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

You may declare the snapshot repository:

```xml
<repositories>
    <repository>
        <id>snapshots-repo</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

Getting Started
---------------

### IndexManager

The IndexManager manages a collection of schemas. A schema is itself a collection of indexes.

```java
import com.qwazr.search.index.IndexManager;

public class MyClass {
    
    public void main(String[] args) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Path indexesDirectory = Paths.get("my_indexes");
        final IndexManager indexManager = new IndexManager(indexesDirectory, executorService);
    }
}
```


### IndexServiceInterface

### Annotations

Javadoc
-------

[The JAVA documentation](../apidocs)

_...documentation in progress..._

