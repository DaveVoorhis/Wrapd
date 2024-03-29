/*
 * BytestreamOutputArray.java
 *
 * Created on 25 April 2004, 02:25
 */

package org.reldb.wrapd.compiler;

/**
 * A BytestreamOutput backed by an array of bytes.
 */
public class BytestreamOutputArray extends BytestreamOutput {

    private final static int minimumCapacity = 1024;

    private byte[] vb = new byte[minimumCapacity];
    private int index = 0;

    /**
     * Reset index.
     */
    public void reset() {
        index = 0;
    }

    /**
     * Get the array of bytes that represents the stream.
     *
     * @return An array of byte.
     */
    public byte[] getBytes() {
        var outArray = new byte[index];
        System.arraycopy(vb, 0, outArray, 0, index);
        return outArray;
    }

    @Override
    public void put(int b) {
        if (index + 1 > vb.length) {
            var newCapacity = (vb.length + 1) * 2;
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE;
            } else if (minimumCapacity > newCapacity) {
                newCapacity = minimumCapacity;
            }
            var newValue = new byte[newCapacity];
            System.arraycopy(vb, 0, newValue, 0, index);
            vb = newValue;
        }
        vb[index++] = (byte) b;
    }

}
