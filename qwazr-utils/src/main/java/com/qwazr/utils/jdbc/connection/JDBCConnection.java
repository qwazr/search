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
package com.qwazr.utils.jdbc.connection;

import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.jdbc.Transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * A connection manager getting database connection from an JDBC url.
 * Take care to set driver and url parameters before using getNewTransaction
 * method.
 * That example show how create an instance of a JDBCConnection using a MySQL
 * database.
 * <pre>
 * JDBCConnection connectionManager = new JDBCConnection();
 * connectionManager.setDriver(&quot;com.mysql.jdbc.Driver&quot;);
 * connectionManager
 * 		.setUrl(&quot;jdbc:mysql://localhost:3306/dbName?autoReconnect=true&quot;);
 * </pre>
 */
public class JDBCConnection extends ConnectionManager {

	private String url;

	private String driver;

	private String username;

	private String password;

	/**
	 * The empty constructor. Used for bean compatibility. Parameters can be
	 * passed using setters.
	 */
	public JDBCConnection() {
		url = null;
		driver = null;
	}

	/**
	 * @param classLoader the ClassLoader to use for object creation
	 * @param driver      The driver class name
	 * @param url         The url used to connect to database
	 * @throws ClassNotFoundException if the class of the driver cannot be found
	 * @throws IllegalAccessException in case of any illegal access
	 * @throws InstantiationException if the driver cannot be constructed
	 */
	public JDBCConnection(final ClassLoader classLoader, final String driver, final String url)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		setDriver(classLoader, driver);
		setUrl(url);
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(ClassLoader classLoader, String driver)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (logger.isLoggable(Level.FINEST))
			logger.finest("New Database instance - Driver: " + driver + " Url: " + url);
		if (driver != null)
			ClassLoaderUtils.findClass(classLoader, driver).newInstance();
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Transaction getNewTransaction(boolean autoCommit, Integer transactionIsolation) throws SQLException {
		return getNewTransaction(autoCommit, transactionIsolation, null);
	}

	/**
	 * Get a new Transaction instance. You can add a suffix on the url used to
	 * establish the database connection.
	 *
	 * @param autoCommit           set to true to enable auto-commit
	 * @param transactionIsolation choose the right isolation level
	 * @param urlSuffix            A suffix added to the url when establishing the database
	 *                             connection
	 * @return a new Transaction instance
	 * @throws SQLException if any JDBC error occurs
	 */
	public Transaction getNewTransaction(boolean autoCommit, Integer transactionIsolation, String urlSuffix)
			throws SQLException {
		String localUrl = url;
		if (urlSuffix != null)
			localUrl += urlSuffix;
		if (logger.isLoggable(Level.FINEST))
			logger.finest("DriverManager.getConnection " + localUrl);
		final Connection cnx ;
		if (username != null || password != null)
			cnx = DriverManager.getConnection(localUrl, username, password);
		else
			cnx = DriverManager.getConnection(localUrl);
		if (transactionIsolation != null)
			cnx.setTransactionIsolation(transactionIsolation);
		cnx.setAutoCommit(autoCommit);
		return new Transaction(cnx, autoCommit, transactionIsolation);
	}

}
