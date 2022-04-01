/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.binder;

import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FieldMapWrapperTest {

	private static FieldMapWrappers wrappers;
	private static FieldMapWrapper<Record> wrapper;

	@Test
	public void test100createWrapper() {
		wrappers = new FieldMapWrappers();
		wrapper = wrappers.get(Record.class);
		Assert.assertNotNull(wrapper);
	}

	@Test
	public void test200newMap() {
		Record record = new Record(RandomUtils.nextLong(), RandomUtils.alphanumeric(10), RandomUtils.nextDouble(),
				RandomUtils.alphanumeric(3), RandomUtils.alphanumeric(3));
		Map<String, ?> map = wrapper.newMap(record);
		Assert.assertNotNull(map);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test300newMapCollection() {
		Record record1 = new Record(RandomUtils.nextLong(), RandomUtils.alphanumeric(10), RandomUtils.nextDouble(),
				RandomUtils.alphanumeric(3), RandomUtils.alphanumeric(3));
		Record record2 = new Record(RandomUtils.nextLong(), RandomUtils.alphanumeric(10), RandomUtils.nextDouble(),
				RandomUtils.alphanumeric(3), RandomUtils.alphanumeric(3));
		List<Map<String, Object>> mapCollection = wrapper.newMapCollection(Arrays.asList(record1, record2));
		Assert.assertNotNull(mapCollection);
		Assert.assertEquals(2, mapCollection.size());
		Assert.assertEquals(record1, mapCollection.get(0));
		Assert.assertEquals(record2, mapCollection.get(1));
	}

	@Test
	public void test400newMapArray() {
		Record record = new Record(RandomUtils.nextLong(), RandomUtils.alphanumeric(10), RandomUtils.nextDouble(),
				RandomUtils.alphanumeric(3), RandomUtils.alphanumeric(3));
		Map<String, ?> map = wrapper.newMap(record);
		Assert.assertNotNull(map);
		Assert.assertEquals(record.title, map.get("title"));
		Assert.assertEquals(record.id, map.get("id"));
	}

	private Map<String, Object> getRandom() {
		Map<String, Object> map = new HashMap<>();
		final String title = RandomUtils.alphanumeric(10);
		final Double price = RandomUtils.nextDouble();
		map.put("id", RandomUtils.nextLong());
		map.put("title", title);
		map.put("price", price);
		LinkedHashMap<String, Double> m = new LinkedHashMap<>();
		m.put(title, price);
		map.put("map", m);
		LinkedHashMap<String, List<Double>> ml = new LinkedHashMap<>();
		ml.put(title, Arrays.asList(price));
		map.put("mapList", ml);
		return map;
	}

	@Test
	public void test500toRecord() throws ReflectiveOperationException, IOException {
		Map map = getRandom();
		Record record = wrapper.toRecord(map);
		Assert.assertNotNull(record);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test501toRecordStringToCollection() throws ReflectiveOperationException, IOException {
		Map map = getRandom();
		map.put("tags", RandomUtils.alphanumeric(5));
		map.put("tagsAbstract", RandomUtils.alphanumeric(6));
		map.put("tagsAbstractList", RandomUtils.alphanumeric(7));
		Record record = wrapper.toRecord(map);
		Assert.assertNotNull(record);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test502toRecordCollectionToCollection() throws ReflectiveOperationException, IOException {
		Map map = getRandom();
		map.put("tags", Arrays.asList(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(5)));
		map.put("tagsAbstract", Arrays.asList(RandomUtils.alphanumeric(6), RandomUtils.alphanumeric(6)));
		map.put("tagsAbstractList", Arrays.asList(RandomUtils.alphanumeric(7), RandomUtils.alphanumeric(7)));
		Record record = wrapper.toRecord(map);
		Assert.assertNotNull(record);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test503toRecordArrayToCollection() throws ReflectiveOperationException, IOException {
		Map map = getRandom();
		map.put("tags", new String[] { RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(5) });
		map.put("tagsAbstract", new String[] { RandomUtils.alphanumeric(6), RandomUtils.alphanumeric(6) });
		map.put("tagsAbstractList", new String[] { RandomUtils.alphanumeric(7), RandomUtils.alphanumeric(7) });
		Record record = wrapper.toRecord(map);
		Assert.assertNotNull(record);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test600collectionToRecords() throws IOException, ReflectiveOperationException {
		Map<String, Object> map1 = getRandom();
		Map<String, Object> map2 = getRandom();
		List<Record> records = wrapper.toRecords(Arrays.asList(map1, map2));
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals(records.get(0), map1);
		Assert.assertEquals(records.get(1), map2);
	}

	@Test
	public void test700arrayToRecords() throws IOException, ReflectiveOperationException {
		Map<String, Object> map1 = getRandom();
		Map<String, Object> map2 = getRandom();
		List<Record> records = wrapper.toRecords(map1, map2);
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals(records.get(0), map1);
		Assert.assertEquals(records.get(1), map2);
	}

	@Test
	public void test801cacheClear() {
		Assert.assertEquals(wrapper, wrappers.get(Record.class));
		wrappers.clear();
		Assert.assertNotEquals(wrapper, wrappers.get(Record.class));
	}

	public static class Record {

		final Long id;
		final String title;
		final Double price;
		final LinkedHashSet<String> tags;
		final Set<String> tagsAbstract;
		final List<String> tagsAbstractList;
		final Map<String, Double> map;
		final Map<String, List<Double>> mapList;

		Record(Long id, String title, Double price, String... tags) {
			this.id = id;
			this.title = title;
			this.price = price;
			if (tags == null || tags.length == 0) {
				this.tags = null;
				this.tagsAbstract = null;
				this.tagsAbstractList = null;
			} else {
				this.tags = new LinkedHashSet<>();
				Collections.addAll(this.tags, tags);
				this.tagsAbstract = new LinkedHashSet<>();
				Collections.addAll(this.tagsAbstract, tags);
				this.tagsAbstractList = new ArrayList<>();
				Collections.addAll(this.tagsAbstractList, tags);
			}
			this.map = new LinkedHashMap<>();
			this.map.put(title, price);
			this.mapList = new LinkedHashMap<>();
			this.mapList.put(title, Arrays.asList(price));
		}

		public Record() {
			this(null, null, null);
		}

		private static boolean equalsStringMap(Object mtags, Collection<String> tags) {
			if (mtags == null)
				return tags == null || tags.isEmpty();
			if (mtags instanceof Collection)
				return CollectionsUtils.equals(tags, (Collection) mtags);
			if (mtags instanceof String)
				return tags.size() == 1 && tags.iterator().next().equals(mtags);
			return CollectionsUtils.equals(tags, (String[]) mtags);
		}

		@Override
		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (object instanceof Record) {
				Record r = (Record) object;
				return Objects.equals(id, r.id) && Objects.equals(title, r.title) &&
						CollectionsUtils.equals(tags, r.tags) && CollectionsUtils.equals(map, r.map);
			}
			if (object instanceof Map) {
				Map m = (Map) object;
				if (!Objects.equals(id, m.get("id")) && Objects.equals(title, m.get("title")))
					return false;
				if (!equalsStringMap(m.get("tags"), tags) && equalsStringMap(m.get("tagsAbstract"), tagsAbstract) &&
						equalsStringMap(m.get("tagsAbstractList"), tagsAbstractList))
					return false;
				if (!CollectionsUtils.equals((Map) m.get("map"), map))
					return false;
				if (!CollectionsUtils.equals((Map) m.get("mapList"), mapList))
					return false;
				return true;
			}
			return false;
		}

	}

	public static class FieldMapWrappers extends FieldMapWrapper.Cache {

		public FieldMapWrappers() {
			super(new HashMap<>());
		}

		@Override
		protected <C> FieldMapWrapper<C> newFieldMapWrapper(Class<C> objectClass) throws NoSuchMethodException {
			final Map<String, FieldSetter> fieldMap = new HashMap<>();
			AnnotationsUtils.browseFieldsRecursive(objectClass, field -> {
				field.setAccessible(true);
				fieldMap.put(field.getName(), FieldSetter.of(field));
			});
			return new FieldMapWrapper<>(fieldMap, objectClass);
		}
	}
}
