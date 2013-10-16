package com.acunu.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Basic construct consisting of two typed objects.
 * 
 * @author abyde
 */
public class Pair<A, B> implements Map.Entry<A, B>, Serializable {
	private static final long serialVersionUID = 1L;
	protected A fst;
	protected B snd;

	/** Public no-arg constructor so we can serialize with MsgPack. */
	public Pair() {
	}

	public Pair(A fst, B snd) {
		this.fst = fst;
		this.snd = snd;
	}

	public Pair<A, B> copy() {
		return new Pair<A, B>(fst, snd);
	}

	/** Less verbose way of making a Pair */
	public static <A,B> Pair<A,B> make(A fst, B snd) {
	    return new Pair<A,B>(fst, snd);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pair other = (Pair) obj;
		if (fst == null) {
			if (other.fst != null)
				return false;
		} else if (fst instanceof Object[] && other.fst instanceof Object[] ? !Arrays.equals((Object[]) fst, (Object[]) other.fst) : !fst.equals(other.fst))
			return false;
		if (snd == null) {
			if (other.snd != null)
				return false;
		} else if (snd instanceof Object[] && other.snd instanceof Object[] ? !Arrays.equals((Object[]) snd, (Object[]) other.snd) : !snd.equals(other.snd))
			return false;
		return true;
	}

	@Override
	public A getKey() {
		return fst;
	}

	@Override
	public B getValue() {
		return snd;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((fst == null) ? 0 : fst.hashCode());
		result = prime * result + ((snd == null) ? 0 : snd.hashCode());
		return result;
	}

	public void setFst(A fst) {
		this.fst = fst;
	}

	public void setKey(A fst) {
		this.fst = fst;
	}

	@Override
	public B setValue(B value) {
		final B old = this.snd;
		this.snd = value;
		return old;
	}

	@Override
	public String toString() {
		if (fst instanceof Object[])
			if (snd instanceof Object[])
				return "(" + Arrays.toString((Object[]) fst) + ", " + Arrays.toString((Object[]) snd) + ")";
			else
				return "(" + Arrays.toString((Object[]) fst) + ", " + snd + ")";
		else
			if (snd instanceof Object[])
				return "(" + fst + ", " + Arrays.toString((Object[]) snd) + ")";
			else
				return "(" + fst + ", " + snd + ")";
	}
}
