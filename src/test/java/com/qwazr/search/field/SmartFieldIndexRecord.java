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

import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.test.units.AbstractIndexTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@Ignore
public class SmartFieldIndexRecord extends AbstractIndexTest<SmartFieldIndexRecord.Record> {

	public SmartFieldIndexRecord() {
		super(Record.class);
	}

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException {
		initIndexService(Record.class);
	}

	@Test
	public void indexDocument() throws IOException, InterruptedException, ReflectiveOperationException {
		Record record = new Record(1);

		long last = indexService.getIndexStatus().num_docs;
		indexService.postDocument(record);
		Assert.assertEquals(last + 1, indexService.getIndexStatus().num_docs, 0);

		Record record2 = indexService.getDocument(1);
		Assert.assertEquals(record, record2);
	}

	@Index(name = "SmartField", schema = "TestQueries")
	static class Record {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.INTEGER)
		final Integer id;

		Record() {
			id = null;
		}

		Record(Integer id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Record))
				return false;
			final Record r = (Record) o;
			return Objects.equals(id, r.id);
		}
	}
}
