/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

class RowIterator implements Iterator<Row> {

	private final Map<String, Integer> columnMap;

	private final ResultSet resultSet;

	private Boolean next;

	RowIterator(Map<String, Integer> columnMap, ResultSet resultSet) throws SQLException {
		this.columnMap = columnMap;
		this.resultSet = resultSet;
		// If the result set is null, "next" is immedialy set to false (and will not be evaluated again)
		this.next = resultSet == null ? false : null;
	}

	@Override
	public boolean hasNext() {
		if (next != null)
			return next;
		try {
			next = resultSet.next();
			return next;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Row next() {
		try {
			if (!hasNext())
				return null;
			next = null; // Set to null, that way "Next" will be reevaluated
			return new Row(columnMap, resultSet);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
