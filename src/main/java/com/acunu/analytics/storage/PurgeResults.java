package com.acunu.analytics.storage;

public class PurgeResults {
    public int numRowsChecked = 0;
    public int numRowsDeleted = 0;
    public int numColsChecked = 0;
    public int numColsDeleted = 0;

	public void absorb(PurgeResults other) {
		numRowsChecked += other.numRowsChecked;
		numRowsDeleted += other.numRowsDeleted;
		numColsChecked += other.numColsChecked;
		numColsDeleted += other.numColsDeleted;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("rows: { checked: ").append(numRowsChecked).append(", deleted: ").append(numRowsDeleted).append(" }");
		builder.append(", cols: { checked: ").append(numColsChecked).append(", deleted: ").append(numColsDeleted).append(" } }");
		return builder.toString();
	}
}