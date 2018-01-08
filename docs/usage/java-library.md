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

QWAZR Search is a JAVA 8 library (JAVA 9 compatibility will be available soon).

The main classes you need are:

- IndexManager: It manages a collection of schemas. A schema is itself a collection of indexes.
- AnnotatedIndexService: This class represents an index and provides all the methods you need to interact it.

The annotations will be used to define the index and the record:

- @Index: defines the name of the index, the name of the schema, and several optional properties.
- @SmartField: defines a field.

```java
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.query.MultiFieldQuery;
import com.qwazr.search.query.QueryParserOperator;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GettingStarted {

	// This class defines both an index, and a record for this index
	@Index(schema = "my_schema", name = "my_index")
	public static class MyIndexRecord {

		@SmartField(name = FieldDefinition.ID_FIELD, index = true, stored = true)
		String id;

		@SmartField(index = true,
				sort = true,
				stored = true,
				analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
				queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
		String title;

		@SmartField(index = true,
				stored = true,
				analyzerClass = SmartAnalyzerSet.EnglishIndex.class,
				queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
		String content;

		@SmartField(index = true, facet = true)
		String[] tags;

		// Public no argument constructor is mandatory
		public MyIndexRecord() {
		}

		MyIndexRecord(String id, String title, String content, String... tags) {
			this.id = id;
			this.title = title;
			this.content = content;
			this.tags = tags;
		}
	}

	@Test
	public void test() throws IOException, URISyntaxException, InterruptedException {

		/* INDEX MANAGER SETUP */

		ExecutorService executorService = Executors.newCachedThreadPool(); // we need a pool of thread
		Path indexesDirectory = Files.createTempDirectory("my_indexes"); // The directory where the indexes are stored
		IndexManager indexManager =
				new IndexManager(indexesDirectory, executorService); // Let's build our index manager

		/* INDEX SETUP */

		AnnotatedIndexService<MyIndexRecord> myIndexService =
				indexManager.getService(MyIndexRecord.class); // Get the service related to our index class (MyIndex)
		myIndexService.createUpdateSchema(); // We create the schema (nothing is done if the schema already exists)
		myIndexService.createUpdateIndex(); // We create the index (nothing is done if the index already exists)
		myIndexService.createUpdateFields(); // We create the fields (nothing is done if the fields already exist)

		/* INDEXING RECORD */

		MyIndexRecord indexRecord1 = new MyIndexRecord("1", "First news", "My first article", "news", "infos");
		MyIndexRecord indexRecord2 = new MyIndexRecord("2", "Second news", "My second article", "news", "infos");
		myIndexService.postDocuments(Arrays.asList(indexRecord1, indexRecord2)); // Let's index them

		/* SEARCH THE INDEX */

		// We create a user query
		MultiFieldQuery multiFieldQuery =
				MultiFieldQuery.of().defaultOperator(QueryParserOperator.AND) // The operator will be AND
						.fieldBoost("title", 3.0f) // The title field has a boost of 3
						.fieldBoost("content", 1.0f) // The content field has a boost of 1
						.queryString("first article") // We look for the terms "my article"
						.build();

		// We also would love to have a facet
		FacetDefinition facet = FacetDefinition.of(10).sort(FacetDefinition.Sort.value_descending).build();

		// We can build our final request
		QueryDefinition queryDefinition =
				QueryDefinition.of(multiFieldQuery).start(0).rows(10).facet("tags", facet).returnedField("*").build();

		// We can now do the search
		ResultDefinition.WithObject<MyIndexRecord> result = myIndexService.searchQuery(queryDefinition);

		// And here is our search result
		assert result.getTotalHits() == 1L; // The number of results
		List<ResultDocumentObject<MyIndexRecord>> records = result.getDocuments(); // Retrieve our found records
		assert indexRecord1.id.equals(
				records.get(0).getRecord().id); // Check we found the right record by checking the ID
		Map<String, Number> facets = result.getFacet("tags"); // Retrieve our facet resuls
		assert facets.get("infos").intValue() == 1; // Check we have our facet result for the tag "infos"
		assert facets.get("news").intValue() == 1; // Same for the tag "news"

		/* FREE RESOURCES */
		indexManager.close(); // IndexManager is closeable (close it only if you will not use the service anymore)
	}

}
```

[Link to the source of the GettingStarted class](https://github.com/qwazr/search/blob/master/src/test/java/com/qwazr/search/docs/GettingStarted.java)

Javadoc
-------

[The JAVA documentation](../apidocs)

_...documentation in progress..._

