package com.acunu.util;

public class SerialiserUtils {
	/**
	 * A mapping of a long to bytes in such a way that byte order is equal to
	 * long order.
	 */
	public static void orderedBytesOfLong(final byte[] bytes, final int offset, final long num) {
		int pos = offset + 8;
		long val = num;
		for (int i = 0; i < 8; i++) {
			byte byt = (byte) (val & 0xff);
			val = val >> 8;
			bytes[--pos] = byt;
		}

		bytes[pos] ^= 0x80;
	}

	/**
	 * Decode a byte-ordered int.
	 */
	public static long longOfOrderedBytes(final byte[] bytes, final int pos) {
		long num = 0;
		for (int i = pos; i < pos + 8; i++) {
			int bNum = bytes[i] & 0xff;
			if (i == pos) {
				bNum ^= 0x80;
			}
			num = (num << 8) + bNum;
		}
		return num;
	}

	/**
	 * A mapping of a double to bytes in such a way that byte order is equal to
	 * double order.
	 */
	public static void orderedBytesOfDouble(final byte[] bytes, final int offset, final double num) {
		long bits = Double.doubleToLongBits(num);
		long sgn = bits >> 63;
		int pos = offset + 8;
		for (int i = 0; i < 8; i++) {
			byte byt = (byte) (bits & 0xff);
			bits = bits >> 8;
			bytes[--pos] = (sgn < 0) ? (byte) (byt ^ 0xff) : byt;
		}

		if (sgn == 0)
			bytes[pos] ^= 0x80;
	}
	
	/**
	 * Decode a byte-ordered double.
	 */
	public static double doubleOfOrderedBytes(final byte[] bytes, final int pos) {
		// sgn is opposite...
		long sgn = 1 + (bytes[pos] >> 7);
		long num = 0;
		for (int i = pos; i < pos + 8; i++) {
			int bNum = bytes[i] & 0xff;
			if (sgn > 0)
				bNum ^= 0xff;
			else if (i == pos) {
				bNum ^= 0x80;
			}
			num = (num << 8) + bNum;
		}
		return Double.longBitsToDouble(num);
	}
}
