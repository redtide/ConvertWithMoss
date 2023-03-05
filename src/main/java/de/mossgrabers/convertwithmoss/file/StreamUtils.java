package de.mossgrabers.convertwithmoss.file;

import de.mossgrabers.tools.ui.Functions;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * Helper class for dealing reading from streams and data inputs.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class StreamUtils
{
    /**
     * Reads and converts 4 bytes to an unsigned integer with least significant bytes first.
     *
     * @param in The input stream
     * @return The converted integer
     * @throws IOException The stream has been closed and the contained input stream does not
     *             support reading after close, or another I/O error occurs.
     */
    public static int readIntLSB (final InputStream in) throws IOException
    {
        int ch1 = in.read ();
        int ch2 = in.read ();
        int ch3 = in.read ();
        int ch4 = in.read ();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException ();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }


    /**
     * Reads and converts 4 bytes to an unsigned integer with least significant bytes first.
     *
     * @param fileAccess The random access file to read from
     * @return The converted integer
     * @throws IOException The stream has been closed and the contained input stream does not
     *             support reading after close, or another I/O error occurs.
     */
    public static int readIntLSB (final RandomAccessFile fileAccess) throws IOException
    {
        int ch1 = fileAccess.read ();
        int ch2 = fileAccess.read ();
        int ch3 = fileAccess.read ();
        int ch4 = fileAccess.read ();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException ();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }


    /**
     * Converts a number of bytes to an unsigned integer with least significant bytes first.
     *
     * @param data The data to convert
     * @return The converted integer
     */
    public static int fromBytesLSB (final byte [] data)
    {
        int number = 0;
        for (int i = 0; i < data.length; i++)
            number |= (data[i] & 0xFF) << 8 * i;
        return number;
    }


    /**
     * Converts a 4 byte float value.
     *
     * @param data The 4 byte array
     * @return The float value
     */
    public static float readFloatLittleEndian (final byte [] data)
    {
        return ByteBuffer.wrap (data).order (ByteOrder.LITTLE_ENDIAN).getFloat ();
    }


    /**
     * Read an LSB 7 bit of a flexible number of bytes.
     *
     * @param in The input stream to read from
     * @return Could not read next byte
     * @throws IOException
     */
    public static int [] read7bitNumberLSB (final InputStream in) throws IOException
    {
        int number = 0;
        int count = 0;

        byte [] value;

        while ((value = in.readNBytes (1)).length > 0)
        {
            final int val = value[0] & 0x7F;
            final int shift = 7 * count;
            number = val << shift | number;

            if ((value[0] & 0x80) == 0)
                break;

            count++;
        }

        return new int []
        {
            number,
            count + 1
        };
    }


    /**
     * Reads a number of bytes from an input stream and interprets it as ASCII text.
     *
     * @param in The stream to read from
     * @param length The length of the text
     * @return The read text
     * @throws IOException
     */
    public static String readASCII (final InputStream in, final int length) throws IOException
    {
        final byte [] buffer = new byte [length];
        if (in.read (buffer) != length)
            throw new IOException ();
        return new String (buffer, StandardCharsets.US_ASCII);
    }


    /**
     * Reads a fixed number of bytes from an input stream and interprets it as ASCII text.
     *
     * @param in The stream to read from
     * @param length The length of the text
     * @return The read text
     * @throws IOException
     */
    public static String readASCII (final DataInput in, final int length) throws IOException
    {
        return readASCII (in, length, StandardCharsets.US_ASCII);
    }


    /**
     * Reads a fixed number of bytes from an input stream and interprets it as ASCII text.
     *
     * @param in The stream to read from
     * @param length The length of the text
     * @param charset The character set to use
     * @return The read text
     * @throws IOException
     */
    public static String readASCII (final DataInput in, final int length, final Charset charset) throws IOException
    {
        final byte [] buffer = new byte [length];
        in.readFully (buffer);
        return new String (buffer, charset);
    }


    /**
     * Reads a 4 byte (LSB) Unix timestamp UTC+1, e.g. "0B 0B 64 4D" is 1298402059 is "22.02.2011
     * 20:14:19".
     *
     * @param in The stream to read from
     * @return The timestamp as a date
     * @throws IOException
     */
    public static Date readTimestampLSB (final InputStream in) throws IOException
    {
        return new Date (readIntLSB (in) * 1000L);
    }


    /**
     * Reads a 4 byte (LSB) Unix timestamp UTC+1, e.g. "0B 0B 64 4D" is 1298402059 (seconds) is
     * "22.02.2011 20:14:19".
     *
     * @param fileAccess The random access file to read from
     * @return The timestamp as a date
     * @throws IOException
     */
    public static Date readTimestampLSB (final RandomAccessFile fileAccess) throws IOException
    {
        return new Date (readIntLSB (fileAccess) * 1000L);
    }


    /**
     * Skip N bytes.
     *
     * @param fileAccess The random access file to read from
     * @param numBytes The number of bytes to skip
     * @throws IOException Could not skip the bytes
     */
    public static void skipNBytes (final RandomAccessFile fileAccess, final int numBytes) throws IOException
    {
        if (fileAccess.skipBytes (numBytes) != numBytes)
            throw new IOException (Functions.getMessage ("IDS_NKI_FILE_CORRUPTED"));
    }
}
