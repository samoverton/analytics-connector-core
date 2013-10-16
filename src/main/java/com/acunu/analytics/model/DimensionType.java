package com.acunu.analytics.model;


public enum DimensionType {
	STRING(String.class, false), ENUM(String.class, false), BAG(String.class, false), PATH(String.class, false), //
	LONG(Long.class, true), TIME(Long.class, true), DOUBLE(Double.class, true), DECIMAL(Double.class, true);

	public final Class<?> rawType;
	public final boolean isRange;
	
	private DimensionType(Class<?> rawType, boolean isRange) {
		this.rawType = rawType;
		this.isRange = isRange;
	}
	
	public static DimensionType parse(String str) {
		if (str == null)
			return null;
		return valueOf(str.toUpperCase().trim());
	}
}

