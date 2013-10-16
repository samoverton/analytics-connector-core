package com.acunu.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.server.ServerException;

/**
 * A serialiser is able to read and write objects to and from ByteBuffers
 * respectively.
 * 
 * @author abyde
 */
public interface Serialiser<X> {

	/**
	 * Read an object from the given ByteBuffer. Must be null-safe (i.e. return
	 * null if bytes is null). Should NOT rewind after a read.
	 */
	X fromBytes(ByteBuffer buf) throws IOException;

	/**
	 * How big will the object be when serialised?
	 */
	int sizeInBytes(X value);

	/**
	 * Construct a byte buffer to contain a given value. Must be null-safe (i.e.
	 * return null if x is null). MUST rewind the buffer.
	 */
	ByteBuffer toBytes(X value);

	/**
	 * Write the given object into the provided buffer. More efficient for
	 * serialising lots of small objects than repeatedly calling toBytes. This
	 * method should not rewind after writing. If that is needed, the calling
	 * class is responsible for doing it. Must NOT rewind the buffer.
	 */
	void write(ByteBuffer buf, X value) throws IOException;

	public static class SerialisationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public SerialisationException(Exception exn) {
			super(exn);
		}
	}
	
	/**
	 * A serialiser that is aware of sizes and streams, and for which toBytes is
	 * a derived operation.
	 * 
	 * @author abyde
	 */
	public static abstract class AbstractLookaheadSerialiser<X> implements
			Serialiser<X> {
		private static Logger logger = LoggerFactory.getLogger(Serialiser.class);
		protected boolean isTrace = logger.isTraceEnabled();

		@Override
		public final ByteBuffer toBytes(X value) {
			if (value == null)
				return null;
			int size = sizeInBytes(value);
			if (isTrace) {
				logger.trace("reserved size " + size);
			}
			ByteBuffer buf = ByteBuffer.allocate(size);
			try {
				write(buf, value);
			} catch (IOException exn) {
				throw new SerialisationException(exn);
			}
			buf.rewind();
			return buf;
		}
		
		public abstract X fromBytes(ByteBuffer buf) throws IOException;

		public abstract int sizeInBytes(X value);

		public abstract void write(ByteBuffer buf, X value) throws IOException;
	}

	/**
	 * An abstract serialiser whose fundamental method is getBytes() and whose
	 * length is unknown. fromBytes is left abstract, but 'retrieveBytes' is
	 * provided to sub-classes which might help -- see the method for more
	 * details.
	 */
	public static abstract class AbstractBytesSerialiser<X> implements Serialiser<X> {
		private static Logger logger = LoggerFactory
				.getLogger(AbstractBytesSerialiser.class);
		private final boolean isTrace = logger.isTraceEnabled();

		private final boolean writeLengthFirst;
		private final int headerSize;

		public AbstractBytesSerialiser() {
			this(false);
		}

		public AbstractBytesSerialiser(boolean writeLengthFirst) {
			this.writeLengthFirst = writeLengthFirst;
			headerSize = (writeLengthFirst ? 4 : 0);
		}

		/**
		 * Read the length, then read that many bytes. If the length is not
		 * pre-written (see constructor flag) then take all bytes to the end of
		 * the buffer. Do not rewind.
		 */
		protected byte[] retrieveBytes(ByteBuffer buf) {
			if (buf == null)
				return null;
			synchronized (buf) {
				int length = 0;
				if (writeLengthFirst)
					length = buf.getInt();
				else
					length = buf.limit() - buf.position();
				byte[] bytes = new byte[length];
				buf.get(bytes);
				return bytes;
			}
		}

		@Override
		public ByteBuffer toBytes(X value) {
			byte[] bytes = getBytes(value);
			if (bytes == null)
				return null;
			if (!writeLengthFirst) {
				return ByteBuffer.wrap(bytes);
			}

			int length = bytes.length + 4;
			if (length > Short.MAX_VALUE)
				throw new IllegalArgumentException("Value too long!  length = " + length);
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.putInt(bytes.length);
			buf.put(bytes);
			buf.rewind();
			return buf;
		}

		/**
		 * Calls getBytes() then writes the resulting array to the provided
		 * ByteBuffer.
		 * 
		 * @throws IllegalArgumentException
		 *             if getBytes(x) == null.
		 * @see com.acunu.util.Serialiser#write(java.nio.ByteBuffer,
		 *      java.lang.Object)
		 */
		@Override
		public void write(ByteBuffer buf, X value) throws IOException {
			if (buf == null)
				throw new IOException("Cannot write to null ByteBuffer");
			byte[] bytes = getBytes(value);
			if (bytes == null)
				throw new IllegalArgumentException("Cannot write null object");
			if (writeLengthFirst)
				buf.putInt(bytes.length);
			buf.put(bytes);
		}

		@Override
		public int sizeInBytes(X value) {
			byte[] bytes = getBytes(value);
			if (bytes == null)
				throw new IllegalArgumentException("Cannot size null object");
			return headerSize + bytes.length;
		}

		/**
		 * Get a representation of an object as a byte array. null -> null
		 */
		public abstract byte[] getBytes(X value);
	}

	/**
	 * A serialiser of Longs.
	 */
	public static final Serialiser<Long> serLong = new AbstractLookaheadSerialiser<Long>() {

		/**
		 * Make sure we advance the position of the buffer.
		 * 
		 * @see com.acunu.util.Serialiser#fromBytes(java.nio.ByteBuffer)
		 */
		@Override
		public Long fromBytes(ByteBuffer buf) {
			if (buf == null)
				return null;
			synchronized (buf) {
				int pos = buf.position();
				long value = SerialiserUtils.longOfOrderedBytes(buf.array(),
						buf.arrayOffset() + pos);
				buf.position(8 + pos);
				return value;
			}
		}

		@Override
		public int sizeInBytes(Long value) {
			return 8;
		}

		@Override
		public void write(ByteBuffer buf, Long value) {
			byte[] bytes = new byte[8];
			SerialiserUtils.orderedBytesOfLong(bytes, 0, value);
			synchronized (buf) {
				buf.put(bytes);
			}
		}

		public String toString() {
			return "serLong";
		}
	};

	/**
	 * A serialiser of Integers.
	 */
	public static final Serialiser<Integer> serInt = new AbstractLookaheadSerialiser<Integer>() {

		@Override
		public Integer fromBytes(ByteBuffer buf) {
			return buf.getInt();
		}

		@Override
		public int sizeInBytes(Integer value) {
			return 4;
		}

		@Override
		public void write(ByteBuffer buf, Integer value) {
			buf.putInt(value);
		}

		public String toString() {
			return "serInt";
		}
	};

	/**
	 * A serialiser of Strings. First write length as a short, then write the
	 * string itself.
	 */
	public static final Serialiser<String> serString = new AbstractBytesSerialiser<String>(
			true) {

		@Override
		public String fromBytes(ByteBuffer buf) {
			// if (buf == null)
			// return null;
			byte[] bytes = retrieveBytes(buf);
			return new String(bytes);
		}

		@Override
		public byte[] getBytes(String value) {
			return value.getBytes();
		}

		public String toString() {
			return "serString";
		}
	};

	/**
	 * A serialiser of Doubles.
	 */
	public static final Serialiser<Double> serDouble = new AbstractLookaheadSerialiser<Double>() {

		/**
		 * Make sure we advance the position of the buffer.
		 * 
		 * @see com.acunu.util.Serialiser#fromBytes(java.nio.ByteBuffer)
		 */
		@Override
		public Double fromBytes(ByteBuffer buf) {
			if (buf == null)
				return null;
			int pos = buf.position();
			Double value = SerialiserUtils.doubleOfOrderedBytes(buf.array(),
					buf.arrayOffset() + pos);
			buf.position(8 + pos);
			return value;
		}

		public int sizeInBytes(Double value) {
			return 8;
		}

		@Override
		public void write(ByteBuffer buf, Double value) {
			byte[] bytes = new byte[8];
			SerialiserUtils.orderedBytesOfDouble(bytes, 0, value);
			synchronized(buf) {
				buf.put(bytes);
			}
		}

		public String toString() {
			return "serDouble";
		}
	};

	public static class CompositeSerialiser extends AbstractLookaheadSerialiser<Object[]> {
		private Serialiser[] ser;

		public CompositeSerialiser(Serialiser... acc) {
			this.ser = acc;
		}

		@Override
		public Object[] fromBytes(ByteBuffer buf) throws IOException {
			int pos = buf.position();
			int numComponents = buf.getInt();
			if (numComponents != ser.length) {
				buf.position(pos);
				throw new ServerException("Invalid number of components, should be " + ser.length + " found " + numComponents);
			}
			Object[] value = new Object[ser.length];
			for (int i = 0; i < ser.length; i++) {
				value[i] = ser[i].fromBytes(buf);
			}
			return value;
		}

		@Override
		public int sizeInBytes(Object[] value) {
			checkSize(value);

			int size = 4;
			for (int i = 0; i < ser.length; i++) {
				size += ser[i].sizeInBytes(value[i]);
			}
			return size;
		}

		@Override
		public void write(ByteBuffer buf, Object[] value) throws IOException {
			checkSize(value);

			buf.putInt(ser.length);
			for (int i = 0; i < ser.length; i++) {
				ser[i].write(buf, value[i]);
			}
		}

		private void checkSize(Object[] value) {
			if (value == null)
				throw new ServerException("Cannot serialise null");
			if (value.length != ser.length)
				throw new ServerException("Invalid number of components, should be " + ser.length + " found " + Arrays.toString(value));
		}
	}

}
