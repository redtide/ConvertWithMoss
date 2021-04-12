// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2021
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.sampleconverter.file.riff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * RIFF Chunks form the building blocks of a RIFF file.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class RIFFChunk implements IChunk
{
    private int                             id;
    private int                             type;
    private long                            size;
    private long                            position;
    private byte []                         data;

    private final Map<RIFFChunk, RIFFChunk> propertyChunks   = new HashMap<> ();
    private final List<RIFFChunk>           collectionChunks = new ArrayList<> ();

    private String                          parserMessage;


    /**
     * Constructor.
     *
     * @param type The type of the chunk
     * @param id The chunk ID
     */
    public RIFFChunk (final int type, final int id)
    {
        this (type, id, -1, -1);
    }


    /**
     * Constructor.
     *
     * @param type The type of the chunk
     * @param id The chunk ID
     * @param size The size of the chunk
     * @param position The position in the file
     */
    public RIFFChunk (final int type, final int id, final long size, final long position)
    {
        this (type, id, size, position, null);
    }


    /**
     * Constructor.
     *
     * @param type The type of the chunk
     * @param id The chunk ID
     * @param size The size of the chunk
     * @param position The position in the file
     * @param propGroup
     */
    public RIFFChunk (final int type, final int id, final long size, final long position, final RIFFChunk propGroup)
    {
        this.id = id;
        this.type = type;
        this.size = size;
        this.position = position;

        if (propGroup == null)
            return;
        this.propertyChunks.putAll (propGroup.propertyChunks);
        this.collectionChunks.addAll (propGroup.collectionChunks);
    }


    /** {@inheritDoc} */
    @Override
    public int getId ()
    {
        return this.id;
    }


    /**
     * Get the type of the chunk.
     *
     * @return Type of chunk
     */
    public int getType ()
    {
        return this.type;
    }


    /**
     * Get the size of the chunk.
     *
     * @return Size of chunk
     */
    public long getSize ()
    {
        return this.size;
    }


    /**
     * Get the position of the chunk in the RIFF file.
     *
     * @return The position within the file
     */
    public long getPosition ()
    {
        return this.position;
    }


    /**
     * Add a property chunk.
     *
     * @param chunk The chunk
     */
    public void putPropertyChunk (final RIFFChunk chunk)
    {
        this.propertyChunks.put (chunk, chunk);
    }


    /**
     * Get a property chunk for the given ID.
     *
     * @param id The ID
     * @return The property chunk
     */
    public RIFFChunk getPropertyChunk (final int id)
    {
        final RIFFChunk chunk = new RIFFChunk (this.type, id);
        return this.propertyChunks.get (chunk);
    }


    /**
     * Get all property chunks.
     *
     * @return The chunks
     */
    public Set<RIFFChunk> propertyChunks ()
    {
        return this.propertyChunks.keySet ();
    }


    /**
     * Add a collection chunk.
     *
     * @param chunk The chunk to add
     */
    public void addCollectionChunk (final RIFFChunk chunk)
    {
        this.collectionChunks.add (chunk);
    }


    /**
     * Get all collection chunks for an ID.
     *
     * @param id The ID
     * @return The chunks
     */
    public List<RIFFChunk> getCollectionChunks (final int id)
    {
        final List<RIFFChunk> array = new ArrayList<> ();
        for (final RIFFChunk chunk: this.collectionChunks)
        {
            if (chunk.id == id)
                array.add (chunk);
        }
        return array;
    }


    /**
     * Iterator to all collection chunks.
     *
     * @return The iterator
     */
    public Iterator<RIFFChunk> collectionChunks ()
    {
        return this.collectionChunks.iterator ();
    }


    /**
     * Sets the data.
     *
     * Note: The array will not be cloned for performance reasons.
     *
     * @param data The data to set
     */
    public void setData (final byte [] data)
    {
        this.data = data;
    }


    /** {@inheritDoc} */
    @Override
    public byte [] getData ()
    {
        return this.data;
    }


    /**
     * Convert 4 bytes to an integer. MSB is first byte.
     *
     * @param offset The offset into the data array
     * @return The integer value
     */
    public int fourBytesAsInt (final int offset)
    {
        return Byte.toUnsignedInt (this.data[offset + 3]) << 24 | Byte.toUnsignedInt (this.data[offset + 2]) << 16 | Byte.toUnsignedInt (this.data[offset + 1]) << 8 | Byte.toUnsignedInt (this.data[offset + 0]);
    }


    /**
     * Convert 2 bytes to an integer MSB is first byte.
     *
     * @param offset The offset into the data array
     * @return The integer value
     */
    public int twoBytesAsInt (final int offset)
    {
        return Byte.toUnsignedInt (this.data[offset + 1]) << 8 | Byte.toUnsignedInt (this.data[offset]);
    }


    /**
     * Convert one byte to an integer.
     *
     * @param offset The offset into the data array
     * @return The integer value
     */
    public int byteAsUnsignedInt (final int offset)
    {
        return Byte.toUnsignedInt (this.data[offset]);
    }


    /**
     * Convert one byte to an integer.
     *
     * @param offset The offset into the data array
     * @return The integer value
     */
    public int byteAsSignedInt (final int offset)
    {
        return this.data[offset];
    }


    /**
     * Read a null terminated string from the data chunk.
     *
     * @param offset The offset into the data array
     * @param defaultValue The value to return if no string is found
     * @return The string
     */
    public String getNullTerminatedString (final int offset, final String defaultValue)
    {
        final StringBuilder sb = new StringBuilder ();

        int counter = offset;
        while (counter < this.data.length)
        {
            if (this.data[counter] == 0)
                return sb.toString ();
            sb.append (Character.valueOf ((char) this.data[counter]));
            counter++;
        }

        // No null terminator detected, return the default value
        return defaultValue;
    }


    /**
     * Convert an integer into 4 bytes. MSB is first byte.
     *
     * @param offset The offset into the data array
     * @param value The integer to convert
     */
    public void intAsFourBytes (final int offset, final int value)
    {
        this.data[offset + 0] = (byte) value;
        this.data[offset + 1] = (byte) (value >> 8);
        this.data[offset + 2] = (byte) (value >> 16);
        this.data[offset + 3] = (byte) (value >> 24);
    }


    /**
     * Convert an integer into 2 bytes. MSB is first byte.
     *
     * @param offset The offset into the data array
     * @param value The integer to convert
     */
    public void intAsTwoBytes (final int offset, final int value)
    {
        this.data[offset + 0] = (byte) value;
        this.data[offset + 1] = (byte) (value >> 8);
    }


    /**
     * Set if the parser runs into a problem but can continue parsing.
     *
     * @param message The notification message to store
     */
    public void setParserMessage (final String message)
    {
        this.parserMessage = message;
    }


    /**
     * Get the notification message, if any.
     *
     * @return The message or null if not set
     */
    public String getParserMessage ()
    {
        return this.parserMessage;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals (final Object another)
    {
        if (another instanceof RIFFChunk)
        {
            final RIFFChunk that = (RIFFChunk) another;
            return that.id == this.id && that.type == this.type;
        }
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode ()
    {
        return this.id;
    }


    /** {@inheritDoc} */
    @Override
    public String toString ()
    {
        return super.toString () + "{" + RiffID.toASCII (this.getType ()) + "," + RiffID.toASCII (this.getId ()) + "}";
    }
}
