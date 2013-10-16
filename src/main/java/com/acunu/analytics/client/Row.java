package com.acunu.analytics.client;

import java.util.Arrays;

/**
 * One row of a query result.
 * 
 * @author twh25
 *
 */
public class Row {
    /** The values of any GROUP BY fields. */
	public Object[] groups;
	
	/** The values of the SELECT columns. */
	public Object[] columns;
	
	public Row(Object[] groups, Object[] columns) {
		this.groups = groups;
		this.columns = columns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columns);
		result = prime * result + Arrays.hashCode(groups);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Row other = (Row) obj;
		if (!Arrays.equals(columns, other.columns))
			return false;
		if (!Arrays.equals(groups, other.groups))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Row [groups=" + Arrays.toString(groups) + ", columns=" + Arrays.toString(columns) + "]";
	}
}
