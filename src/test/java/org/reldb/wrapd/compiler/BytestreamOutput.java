/*
 * BytestreamOutput.java
 *
 * Based, in part, on components of SleepyCat's Java Berkeley DB.
 *
 * Created on 24 April 2004, 19:24
 */

package org.reldb.wrapd.compiler;

/**
 * A class to support streaming ValueS into streams of bytes.
 */
public abstract class BytestreamOutput {

    /**
     * Output a byte.
     *
     * @param b Value written.
     */
    public abstract void put(int b);

    /**
     * Write an unsigned byte.
     *
     * @param val Value written.
     */
    private void putUnsignedByte(int val) {
        put(val);
    }

    /**
     * Output an array of bytes.
     *
     * @param b Value written.
     */
    public void put(byte[] b) {
        for (byte value : b)
            putUnsignedByte(value);
    }

    /**
     * Output a subsection of an array of bytes.
     *
     * @param b Array of byte to output.
     * @param offset Offset in array to start at; zero based.
     * @param length Number of bytes to output.
     */
    public void put(byte[] b, int offset, int length) {
        var count = 0;
        for (var i = offset; i < b.length && count++ < length; i++)
            putUnsignedByte(b[i]);
    }

    /**
     * Writes an unsigned short.
     */
    private void putUnsignedShort(short val) {
        putUnsignedByte((byte) (val >>> 8));
        putUnsignedByte((byte) val);
    }

    /**
     * Writes an unsigned int
     */
    private void putUnsignedInt(int val) {
        putUnsignedByte((byte) (val >>> 24));
        putUnsignedByte((byte) (val >>> 16));
        putUnsignedByte((byte) (val >>> 8));
        putUnsignedByte((byte) val);
    }

    /**
     * Output unsigned long
     */
    private void putUnsignedLong(long val) {
        putUnsignedByte((byte) (val >>> 56));
        putUnsignedByte((byte) (val >>> 48));
        putUnsignedByte((byte) (val >>> 40));
        putUnsignedByte((byte) (val >>> 32));
        putUnsignedByte((byte) (val >>> 24));
        putUnsignedByte((byte) (val >>> 16));
        putUnsignedByte((byte) (val >>> 8));
        putUnsignedByte((byte) val);
    }

    /**
     * Write a String.
     *
     * @param val Value written.
     */
    public final void putString(String val) {
        put(val.getBytes());
        putUnsignedByte((byte) 0);
    }

    /**
     * Write an array of bytes.
     *
     * @param b Value written.
     */
    public final void putbytes(byte[] b) {
        putInt(b.length);
        put(b);
    }

    /**
     * Write a char.
     *
     * @param val Value written.
     */
    public final void putChar(char val) {
        putUnsignedByte((byte) (val >>> 8));
        putUnsignedByte((byte) val);
    }

    /**
     * Write a Character.
     *
     * @param val Value written.
     */
    public final void putChar(Character val) {
        putChar(val.charValue());
    }

    /**
     * Write a boolean.
     *
     * @param val Value written.
     */
    public final void putBoolean(boolean val) {
        putUnsignedByte((byte) (val ? 1 : 0));
    }

    /**
     * Write a Boolean.
     *
     * @param val Value written.
     */
    public final void putBoolean(Boolean val) {
        putBoolean(val.booleanValue());
    }

    /**
     * Writes a byte.
     *
     * @param b Value written.
     */
    public final void putByte(int b) {
        if (b < 0)
            b &= (byte) ~0x80;
        else
            b |= (byte) 0x80;
        putUnsignedByte(b);
    }

    /**
     * Writes a Byte.
     *
     * @param b Value written.
     */
    public final void putByte(Byte b) {
        putByte(b.byteValue());
    }

    /**
     * Writes a short.
     *
     * @param s Value written.
     */
    public final void putShort(int s) {
        if (s < 0)
            s &= (short) ~0x8000;
        else
            s |= (short) 0x8000;
        putUnsignedShort((short) s);
    }

    /**
     * Writes a Short.
     *
     * @param s Value written.
     */
    public final void putShort(Short s) {
        putShort(s.shortValue());
    }

    /**
     * Writes an int.
     *
     * @param val Value written.
     */
    public final void putInt(long val) {
        if (val < 0)
            val &= ~0x80000000;
        else
            val |= 0x80000000;
        putUnsignedInt((int) val);
    }

    /**
     * Writes an Integer.
     *
     * @param i Value written.
     */
    public final void putInteger(Integer i) {
        putInt(i);
    }

    /**
     * Writes a long.
     *
     * @param val Value written.
     */
    public final void putLong(long val) {
        if (val < 0)
            val &= ~0x8000000000000000L;
        else
            val |= 0x8000000000000000L;
        putUnsignedLong(val);
    }

    /**
     * Writes a Long.
     *
     * @param val Value written.
     */
    public final void putLong(Long val) {
        putLong(val.longValue());
    }

    /**
     * Writes a float.
     *
     * @param val Value written.
     */
    public final void putFloat(float val) {
        putUnsignedInt(Float.floatToIntBits(val));
    }

    /**
     * Writes a Float.
     *
     * @param val Value written.
     */
    public final void putFloat(Float val) {
        putFloat(val.floatValue());
    }

    /**
     * Writes a double.
     *
     * @param val Value written.
     */
    public final void putDouble(double val) {
        putUnsignedLong(Double.doubleToLongBits(val));
    }

    /**
     * Writes a Double.
     *
     * @param val Value written.
     */
    public final void putDouble(Double val) {
        putDouble(val.doubleValue());
    }
}