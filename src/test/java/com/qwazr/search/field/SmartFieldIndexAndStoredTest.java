/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.TermQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ExceptionUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class SmartFieldIndexAndStoredTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException {
		initIndexManager();
	}

	<T extends Record> void indexDocument(T record, Class<T> recordClass) throws Exception {
		final AnnotatedIndexService<T> indexService = initIndexService(recordClass);

		// We insert the document
		long last = indexService.getIndexStatus().num_docs;
		indexService.postDocument(record);
		Assert.assertEquals(last + 1, indexService.getIndexStatus().num_docs, 0);

		// We check that we found it
		T record2 = indexService.getDocument(record.getId());
		Assert.assertEquals(record, record2);

		// We should not find it using storeId
		try {
			indexService.searchQuery(QueryDefinition.of(new TermQuery("storedId", record.getId())).build());
			Assert.fail("Exception not thrown");
		} catch (WebApplicationException e) {
			Assert.assertTrue(ExceptionUtils.getRootCause(e).getMessage().contains("storedId"));
		}

		// We should also found it using the secondId
		Assert.assertEquals(record, indexService.searchQuery(QueryDefinition.of(new TermQuery("id2", record.getId()))
				.returnedField("*")
				.build()).getDocuments().get(0).record);
	}

	@Test
	public void indexKeyString() throws Exception {
		indexDocument(new RecordString(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6)), RecordString.class);
	}

	@Test
	public void indexKeyInteger() throws Exception {
		indexDocument(new RecordInteger(RandomUtils.nextInt(), RandomUtils.nextInt()), RecordInteger.class);
	}

	@Test
	public void indexKeyPrimitiveInteger() throws Exception {
		indexDocument(new RecordPrimitiveInteger(RandomUtils.nextInt(), RandomUtils.nextInt()),
				RecordPrimitiveInteger.class);
	}

	@Test
	public void indexKeyLong() throws Exception {
		indexDocument(new RecordLong(RandomUtils.nextLong(), RandomUtils.nextLong()), RecordLong.class);
	}

	@Test
	public void indexKeyPrimitiveLong() throws Exception {
		indexDocument(new RecordPrimitiveLong(RandomUtils.nextLong(), RandomUtils.nextLong()),
				RecordPrimitiveLong.class);
	}

	@Test
	public void indexKeyFloat() throws Exception {
		indexDocument(new RecordFloat(RandomUtils.nextFloat(), RandomUtils.nextFloat()), RecordFloat.class);
	}

	@Test
	public void indexKeyPrimitiveFloat() throws Exception {
		indexDocument(new RecordPrimitiveFloat(RandomUtils.nextFloat(), RandomUtils.nextFloat()),
				RecordPrimitiveFloat.class);
	}

	@Test
	public void indexKeyDouble() throws Exception {
		indexDocument(new RecordDouble(RandomUtils.nextDouble(), RandomUtils.nextDouble()), RecordDouble.class);
	}

	@Test
	public void indexKeyPrimitiveDouble() throws Exception {
		indexDocument(new RecordPrimitiveDouble(RandomUtils.nextDouble(), RandomUtils.nextDouble()),
				RecordPrimitiveDouble.class);
	}

	static abstract class Record<T> {

		abstract T getId();

		abstract T getStoredId();

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Record))
				return false;
			final Record r = (Record) o;
			return Objects.equals(getId(), r.getId()) && Objects.equals(getStoredId(), r.getStoredId());
		}
	}

	@Index(name = "SmartFieldString", schema = "TestQueries")
	static public class RecordString extends Record<String> {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.TEXT, index = true, stored = true)
		final public String id;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
		final public String id2;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, stored = true)
		final public String storedId;

		public RecordString() {
			this(null, null);
		}

		RecordString(String id, String storedId) {
			this.id = id2 = id;
			this.storedId = storedId;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldInteger", schema = "TestQueries")
	static public class RecordInteger extends Record<Integer> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.INTEGER,
				index = true,
				stored = true)
		final public Integer id;

		@SmartField(type = SmartFieldDefinition.Type.INTEGER, index = true)
		final public Integer id2;

		@SmartField(type = SmartFieldDefinition.Type.INTEGER, stored = true)
		final public Integer storedId;

		public RecordInteger() {
			this(null, null);
		}

		RecordInteger(Integer id, Integer storedId) {
			this.id = id2 = id;
			this.storedId = storedId;
		}

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public Integer getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldPrimitiveInteger", schema = "TestQueries")
	static public class RecordPrimitiveInteger extends Record<Integer> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.INTEGER,
				index = true,
				stored = true)
		final public int id;

		@SmartField(type = SmartFieldDefinition.Type.INTEGER, index = true)
		final public int id2;

		@SmartField(type = SmartFieldDefinition.Type.INTEGER, stored = true)
		final public int storedId;

		public RecordPrimitiveInteger() {
			this(null, null);
		}

		RecordPrimitiveInteger(Integer id, Integer storedId) {
			this.id = id2 = id == null ? 0 : id;
			this.storedId = storedId == null ? 0 : storedId;
		}

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public Integer getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldLong", schema = "TestQueries")
	static public class RecordLong extends Record<Long> {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, index = true, stored = true)
		final public Long id;

		@SmartField(type = SmartFieldDefinition.Type.LONG, index = true)
		final public Long id2;

		@SmartField(type = SmartFieldDefinition.Type.LONG, stored = true)
		final public Long storedId;

		public RecordLong() {
			this(null, null);
		}

		RecordLong(Long id, Long storedId) {
			this.id = id2 = id;
			this.storedId = storedId;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public Long getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldPrimitiveLong", schema = "TestQueries")
	static public class RecordPrimitiveLong extends Record<Long> {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, index = true, stored = true)
		final public long id;

		@SmartField(type = SmartFieldDefinition.Type.LONG, index = true)
		final public long id2;

		@SmartField(type = SmartFieldDefinition.Type.LONG, stored = true)
		final public long storedId;

		public RecordPrimitiveLong() {
			this(null, null);
		}

		RecordPrimitiveLong(Long id, Long storedId) {
			this.id = id2 = id == null ? 0 : id;
			this.storedId = storedId == null ? 0 : storedId;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public Long getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldFloat", schema = "TestQueries")
	static public class RecordFloat extends Record<Float> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.FLOAT,
				index = true,
				stored = true)
		final public Float id;

		@SmartField(type = SmartFieldDefinition.Type.FLOAT, index = true)
		final public Float id2;

		@SmartField(type = SmartFieldDefinition.Type.FLOAT, stored = true)
		final public Float storedId;

		public RecordFloat() {
			this(null, null);
		}

		RecordFloat(Float id, Float storedId) {
			this.id = id2 = id;
			this.storedId = storedId;
		}

		@Override
		public Float getId() {
			return id;
		}

		@Override
		public Float getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldPrimitiveFloat", schema = "TestQueries")
	static public class RecordPrimitiveFloat extends Record<Float> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.FLOAT,
				index = true,
				stored = true)
		final public float id;

		@SmartField(type = SmartFieldDefinition.Type.FLOAT, index = true)
		final public float id2;

		@SmartField(type = SmartFieldDefinition.Type.FLOAT, stored = true)
		final public float storedId;

		public RecordPrimitiveFloat() {
			this(null, null);
		}

		RecordPrimitiveFloat(Float id, Float storedId) {
			this.id = id2 = id == null ? Float.NaN : id;
			this.storedId = storedId == null ? Float.NaN : storedId;
		}

		@Override
		public Float getId() {
			return id;
		}

		@Override
		public Float getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldDouble", schema = "TestQueries")
	static public class RecordDouble extends Record<Double> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.DOUBLE,
				index = true,
				stored = true)
		final public Double id;

		@SmartField(type = SmartFieldDefinition.Type.DOUBLE, index = true)
		final public Double id2;

		@SmartField(type = SmartFieldDefinition.Type.DOUBLE, stored = true)
		final public Double storedId;

		public RecordDouble() {
			this(null, null);
		}

		RecordDouble(Double id, Double storedId) {
			this.id = id2 = id;
			this.storedId = storedId;
		}

		@Override
		public Double getId() {
			return id;
		}

		@Override
		public Double getStoredId() {
			return storedId;
		}
	}

	@Index(name = "SmartFieldPrimitiveDouble", schema = "TestQueries")
	static public class RecordPrimitiveDouble extends Record<Double> {

		@SmartField(name = FieldDefinition.ID_FIELD,
				type = SmartFieldDefinition.Type.DOUBLE,
				index = true,
				stored = true)
		final public double id;

		@SmartField(type = SmartFieldDefinition.Type.DOUBLE, index = true)
		final public double id2;

		@SmartField(type = SmartFieldDefinition.Type.DOUBLE, stored = true)
		final public double storedId;

		public RecordPrimitiveDouble() {
			this(null, null);
		}

		RecordPrimitiveDouble(Double id, Double storedId) {
			this.id = id2 = id == null ? Double.NaN : id;
			this.storedId = storedId == null ? Double.NaN : storedId;
		}

		@Override
		public Double getId() {
			return id;
		}

		@Override
		public Double getStoredId() {
			return storedId;
		}
	}
}
