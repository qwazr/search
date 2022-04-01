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

import com.qwazr.utils.jdbc.connection.ConnectionManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an SQL query. In JDBC view, a query contains at least a
 * PreparedStatement. It can also contains a ResultSet. Statement and ResultSet
 * are automatically closed when Query or Transaction is closed.
 * The most important behavior is to return a list of Pojo instead of a
 * ResultSet.
 * The example show how to use it.
 * <pre>
 * Transaction transaction = null;
 * try {
 *   // Obtain a new transaction from the ConnectionManager
 *   transaction = connectionManager.getNewTransaction(false,
 *                             javax.sql.Connection.TRANSACTION_READ_COMMITTED);
 *   // Start a new Query
 *   Query query = transaction.prepare(&quot;SELECT * FROM MyTable WHERE status=?&quot;);
 *   query.getStatement().setString(1, &quot;open&quot;);
 *   query.setFirstResult(0);
 *   query.setMaxResults(10);
 *
 *   // Get the result
 *   List&lt;MyPojo&gt; myPojoList = query.getResultList(MyPojo.class));
 *
 *   // do everything you need
 *
 * } finally {
 *   // Release the transaction
 *   if (transaction != null)
 *     transaction.close();
 * }
 * </pre>
 */
public class Query {

    private ResultSet resultSet;
    private HashMap<Class<?>, List<?>> resultListMap;
    private PreparedStatement statement;
    private int firstResult;
    private int maxResults;

    final static protected Logger logger = Logger.getLogger(Query.class.getCanonicalName());

    protected Query(PreparedStatement statement) {
        this.statement = statement;
        firstResult = 0;
        maxResults = -1;
        resultListMap = new HashMap<Class<?>, List<?>>();
    }

    /**
     * @param firstResult the position of the first result
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    /**
     * @param maxResults the maximum number of rows returned
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Close all component of that query (ResultSet and Statement)
     */
    protected void closeAll() {
        ConnectionManager.close(resultSet, statement, null);
    }

    private static class MethodColumnIndex {
        private int columnIndex;
        private Method method;

        private MethodColumnIndex(int columnIndex, Method method) {
            this.columnIndex = columnIndex;
            this.method = method;
        }

        private void invoke(Object bean, ResultSet resultSet) throws Exception {
            if (method == null)
                throw new Exception("No method found for column " + columnIndex);
            final Object colObject = resultSet.getObject(columnIndex);
            try {
                if (colObject != null)
                    method.invoke(bean, colObject);
            }
            catch (Exception e) {
                throw new Exception("Error on column " + columnIndex + " method " + method.getName() +
                        " object class is " + colObject.getClass().getName(), e);
            }
        }
    }

    private <T> List<T> createBeanList(Class<T> beanClass) throws Exception {
        // Find related methods and columns
        ResultSetMetaData rs = resultSet.getMetaData();
        int columnCount = rs.getColumnCount();
        BeanInfo beanInfo;
        beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        ArrayList<MethodColumnIndex> methods = new ArrayList<MethodColumnIndex>();

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Search properties for bean " + beanClass.getSimpleName());
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getColumnLabel(i);
            for (PropertyDescriptor propDesc : props) {
                if (propDesc.getWriteMethod() != null && propDesc.getName().equalsIgnoreCase(columnName)) {
                    methods.add(new MethodColumnIndex(i, propDesc.getWriteMethod()));
                    if (logger.isLoggable(Level.FINEST))
                        logger.finest(
                                "Found property \"" + propDesc.getName() + "\" for column name \"" + columnName + "\"");
                    break;
                }
            }
        }
        // Create bean list
        List<T> list = new ArrayList<T>();
        moveToFirstResult();
        int limit = maxResults;
        while (resultSet.next() && limit-- != 0) {
            @SuppressWarnings("unchecked") T bean =
                    (T) Beans.instantiate(beanClass.getClassLoader(), beanClass.getCanonicalName());
            for (MethodColumnIndex methodColumnIndex : methods)
                methodColumnIndex.invoke(bean, resultSet);
            list.add(bean);
        }
        return list;
    }

    private void moveToFirstResult() throws SQLException {
        if (firstResult == 0)
            return;
        switch (statement.getResultSetType()) {
            case ResultSet.TYPE_FORWARD_ONLY:
                int i = firstResult;
                while (i-- > 0)
                    resultSet.next();
                break;
            default:
                resultSet.absolute(firstResult);
                break;
        }
    }

    private static LinkedHashMap<String, Integer> buildColumnMap(ResultSet resultSet) throws SQLException {
        LinkedHashMap<String, Integer> columnMap = new LinkedHashMap<String, Integer>();
        if (resultSet == null)
            return columnMap;
        ResultSetMetaData rs = resultSet.getMetaData();
        if (rs == null)
            return columnMap;
        int columnCount = rs.getColumnCount();
        for (int i = 0; i < columnCount; i++)
            columnMap.put(rs.getColumnLabel(i + 1), i);
        return columnMap;
    }

    private static List<Row> createRowList(ResultSet resultSet, int limit) throws SQLException {
        LinkedHashMap<String, Integer> columnMap = buildColumnMap(resultSet);
        ArrayList<Row> rows = new ArrayList<Row>();
        while (resultSet.next() && limit-- != 0)
            rows.add(new Row(columnMap, resultSet));
        return rows;
    }

    @NonNull
    private List<Row> createRowList(int limit) throws SQLException {
        moveToFirstResult();
        return createRowList(resultSet, limit);
    }

    public Iterator<Row> getRowIterator() {
        try {
            checkResultSet();
            return new RowIterator(buildColumnMap(resultSet), resultSet);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the PreparedStatement used by that Query
     *
     * @return a PreparedStatement
     */
    public PreparedStatement getStatement() {
        return statement;
    }

    /**
     * Release the last ResultSet (if any) and the last ResultList.
     */
    public void reUse() {
        if (resultSet != null) {
            ConnectionManager.close(resultSet, null, null);
            resultSet = null;
        }
        resultListMap.clear();
    }

    private void checkResultSet() throws SQLException {
        if (resultSet != null)
            return;
        if (maxResults != -1)
            statement.setFetchSize(maxResults);
        resultSet = statement.executeQuery();
    }

    public <T> T getFirstResult(Class<T> beanClass) throws Exception {
        List<T> list = getResultList(beanClass);
        if (list == null || list.isEmpty())
            return null;
        return list.get(0);
    }

    /**
     * Returns the list of POJO. The list is cached. Every subsequent call
     * returns the same list.
     *
     * @param beanClass The class name of POJO returned in the list
     * @param <T>       the type of the returned object
     * @return a list of POJO
     * @throws SQLException if any JDBC error occurs
     */
    public <T> List<T> getResultList(Class<T> beanClass) throws Exception {
        @SuppressWarnings("unchecked") List<T> resultList = (List<T>) resultListMap.get(beanClass);
        if (resultList != null)
            return resultList;
        checkResultSet();
        resultList = createBeanList(beanClass);
        resultListMap.put(beanClass, resultList);
        return resultList;
    }

    /**
     * @return a list of Row object.
     * @throws SQLException if any JDBC error occurs
     */
    public List<Row> getResultList() throws SQLException {
        checkResultSet();
        return createRowList(maxResults);
    }

    /**
     * @return the first result, or null if there were no result
     * @throws SQLException if any JDBC error occurs
     */
    public Row getFirstResult() throws SQLException {
        checkResultSet();
        List<Row> rows = createRowList(1);
        if (rows.isEmpty())
            return null;
        return rows.get(0);
    }

    /**
     * Do a PreparedStatement.executeUpdate(). A convenient way to execute an
     * INSERT/UPDATE/DELETE SQL statement.
     *
     * @return a row count
     * @throws SQLException if any JDBC error occurs
     */
    public int update() throws SQLException {
        return statement.executeUpdate();
    }

    /**
     * Returns the generated keys after an insert statement
     *
     * @return the list of generated keys
     * @throws SQLException if any JDBC error occurs
     */
    public List<Row> getGeneratedKeys() throws SQLException {
        return createRowList(statement.getGeneratedKeys(), -1);
    }

    /**
     * FirstResult and MaxResults parameters are ignored.
     *
     * @return the number of row found for a select
     * @throws SQLException if any JDBC error occurs
     */
    public int getResultCount() throws SQLException {
        checkResultSet();
        resultSet.last();
        return resultSet.getRow();
    }

    /**
     * Get the ResultSet used by that Query.
     *
     * @return the JDBC ResultSet
     * @throws SQLException if any JDBC error occurs
     */
    public ResultSet getResultSet() throws SQLException {
        checkResultSet();
        return resultSet;
    }
}
