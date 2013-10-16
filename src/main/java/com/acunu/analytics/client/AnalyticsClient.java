package com.acunu.analytics.client;

import java.io.IOException;

import com.acunu.analytics.storage.PurgeResults;

public interface AnalyticsClient {

	/**
	 * Create a table according to the given AQL definition.
	 */
	public void createTable(String tableDefString) throws IOException;

	/**
	 * Create a cube according to the given AQL definition.
	 */
	public void createCube(String aql) throws IOException;

	/**
	 * Drop a table.
	 */
	public void dropTable(String tableName) throws IOException;

	/**
	 * Drop a table.
	 */
	public void dropTableIfExists(String tableName) throws IOException;

	/**
	 * Clean a segment of counters.
	 */
	public PurgeResults clean(String table, Long minTime, long maxTime) throws IOException;

	/**
	 * Perform a query.
	 */
	public Iterable<Row> query(String aqlQuery) throws IOException;
}
