/**
 * @brief   Provides a class that is able to parse a BMP file and split it into more interesting data
 * @class   DAI
 * @pw      1
 * @authors Pedro Alves da Silva, Gonçalo Heleno Carvalheiro
 * Copyright (c) 2024
 */

package ch.heigvd.dai;

import ch.heigvd.dai.exceptions.BmpFileException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BmpFile
{
	private static final String[] VALID_MAGICS = {
		"BM",
		"BA",
		"CI",
		"CP",
		"IC",
		"PT"
	};
	public static final int MIN_MESSAGE_LENGTH = 1;
	public static final int MAX_MESSAGE_LENGTH = 0x1FFFFFFF; // 29 bits. Last 3 bits are for bits-per-byte
	public static final int MIN_BITS_PER_BYTE = 1;
	public static final int MAX_BITS_PER_BYTE = 8;
	private static final int BITS_PER_BYTE_SHIFT = 29;
	private static final int MAGIC_BYTES_LEN = 2;
	private static final int HEADER_FIELDS_LEN = 4;
	private static final int HEADER_LEN = MAGIC_BYTES_LEN + HEADER_FIELDS_LEN * 3;
	private boolean _hasMessage;
	private byte[] _pixelArray;
	private int _fileSize; // BMP files, as per their header, cannot be bigger than 2^32 bytes
	private int _pixelArrayOffset;
	private int _messageLength;
	private int _bitsPerByte;
	private final String _bmpFilePath;

	/**
	 * Parses and splits a bitmap file into useful data
	 *
	 * @param bmpFilePath Path to bitmap file
	 * @throws BmpFileException Error raised in case the bitmap file is not valid
	 * @throws IOException      IO error when handling bitmap file
	 */
	public BmpFile(String bmpFilePath) throws BmpFileException, IOException
	{
		try (FileInputStream bmpInputStream = new FileInputStream(bmpFilePath);
		     BufferedInputStream bufferedInputStream = new BufferedInputStream(bmpInputStream))
		{
			readBitmapHeader(bufferedInputStream);
			readPixelArray(bufferedInputStream);
			_bmpFilePath = bmpFilePath;
		}
	}

	/**
	 * Returns whether this file has a hidden message inside
	 *
	 * @return True if a message is present, false if not
	 */
	public boolean hasMessage()
	{
		return _hasMessage;
	}

	/**
	 * Returns the hidden message's length
	 *
	 * @return Hidden message's length if one is present, or 0 if no message is hidden
	 */
	public int getMessageLength()
	{
		return _messageLength;
	}

	/**
	 * Returns the number of bits belonging to the hidden message per data byte
	 *
	 * @return Number of bits per data byte
	 */
	public int getBitsPerByte()
	{
		return _bitsPerByte;
	}

	/**
	 * Changes the pixel array data, hidden message length, and number of bits per data byte
	 *
	 * @param data          New pixel array
	 * @param messageLength New message length
	 * @param bitsPerByte   Number of bits per data byte
	 */
	public void setData(byte[] data, int messageLength, int bitsPerByte)
	{
		if (data.length != _pixelArray.length)
		{
			throw new BmpFileException("Incoherent data length");
		}

		if (messageLength > data.length)
		{
			throw new BmpFileException("Incoherent message length");
		}

		// We only accept multiples of 2
		if (bitsPerByte != 1 && bitsPerByte % 2 != 0)
		{
			throw new BmpFileException("Invalid bits per byte");
		}

		_pixelArray = data.clone();
		_bitsPerByte = bitsPerByte;
		_messageLength = messageLength;
	}

	/**
	 * Returns the pixel array that was read from the BMP file
	 *
	 * @return Pixel array
	 * @implNote Function returns a copy of the pixel array
	 */
	public byte[] getPixelArray()
	{
		return _pixelArray.clone();
	}

	/**
	 * Dumps the pixel array into the provided output file
	 *
	 * @param outputFilePath Path to the output file. Must not be the same file as the input file
	 * @throws IOException Raised in case an error occurs with either the input or output file
	 * @implNote Original input file must still exist
	 */
	public void saveFile(String outputFilePath) throws IOException
	{
		try (FileInputStream bmpInputStream = new FileInputStream(_bmpFilePath);
		     BufferedInputStream bufferedInputStream = new BufferedInputStream(bmpInputStream);
		     FileOutputStream bmpOutputStream = new FileOutputStream(outputFilePath);
		     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(bmpOutputStream))
		{
			// Write unaltered header (magic bytes + file size)
			bufferedOutputStream.write(bufferedInputStream.readNBytes(MAGIC_BYTES_LEN + HEADER_FIELDS_LEN));

			// Write hidden message length
			ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_FIELDS_LEN);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			int rawBitsAndLen = ((_bitsPerByte - 1) << BITS_PER_BYTE_SHIFT) | _messageLength;
			byteBuffer.putInt(rawBitsAndLen);
			bufferedOutputStream.write(byteBuffer.array());

			// Compensate the fact we didn't read from the input file
			bufferedInputStream.skipNBytes(HEADER_FIELDS_LEN);

			// Data offset
			bufferedOutputStream.write(bufferedInputStream.readNBytes(HEADER_FIELDS_LEN));

			// Additional headers
			bufferedOutputStream.write(bufferedInputStream.readNBytes(_fileSize - _pixelArray.length - HEADER_LEN));

			// Write full pixel array
			bufferedOutputStream.write(_pixelArray);
			bufferedOutputStream.flush();
		}
	}

	/**
	 * Checks whether the provided magic bytes are valid for a bitmap file
	 *
	 * @param magicBytes Buffer containing the magic bytes
	 * @return True if magic is valid, false if not
	 */
	private boolean isValidMagic(byte[] magicBytes)
	{
		boolean validMagic = false;
		for(String acceptedMagic : VALID_MAGICS)
		{
			if(Arrays.equals(acceptedMagic.getBytes(),
						     0,
						     MAGIC_BYTES_LEN,
						     magicBytes,
							 0,
							 MAGIC_BYTES_LEN))
			{
				validMagic = true;
				break;
			} /* if */
		} /* for */
		
		return validMagic;
	}

	/**
	 * Reads and checks the file's header
	 *
	 * @param fileBuffer Buffer to read from
	 * @throws BmpFileException Thrown in case the function fails to read the file's header
	 */
	private void readBitmapHeader(BufferedInputStream fileBuffer) throws BmpFileException
	{
		byte[] magicBytes = new byte[MAGIC_BYTES_LEN];
		ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_FIELDS_LEN);
		
		// Bitmap headers are little endian
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		try
		{
			// Check magic bytes
			if(MAGIC_BYTES_LEN != fileBuffer.read(magicBytes, 0, MAGIC_BYTES_LEN)
			|| !isValidMagic(magicBytes))
			{
				throw new BmpFileException("Invalid magic bytes");
			} /* if */

			// Get file size
			byteBuffer.put(fileBuffer.readNBytes(HEADER_FIELDS_LEN));
			byteBuffer.rewind();
			_fileSize = byteBuffer.getInt();

			// Get the hidden message length (we expect this to be 0 if no message is hidden)
			byteBuffer.rewind();
			byteBuffer.put(fileBuffer.readNBytes(HEADER_FIELDS_LEN));
			byteBuffer.rewind();
			int rawBitsAndLength = byteBuffer.getInt();
			_messageLength = rawBitsAndLength & MAX_MESSAGE_LENGTH;
			_bitsPerByte = rawBitsAndLength >> BITS_PER_BYTE_SHIFT;
			// Happens when MSb is 1 (sign bit)
			if (_bitsPerByte < 0)
			{
				_bitsPerByte *= -1;
			}
			_bitsPerByte += 1; // Compensate for the fact we have a range from 1 to 8
			_hasMessage = _messageLength > 0;

			// Get the offset to start reading the pixel array from
			byteBuffer.rewind();
			byteBuffer.put(fileBuffer.readNBytes(HEADER_FIELDS_LEN));
			byteBuffer.rewind();
			_pixelArrayOffset = byteBuffer.getInt();
		}
		catch (BufferUnderflowException e)
		{
			throw new BmpFileException("Not enough bytes to read the entire header");
		}
		catch (IOException e)
		{
			throw new BmpFileException(e.getMessage());
		} /* try */
	}

	/**
	 * Reads the bitmap file's pixel array
	 *
	 * @param fileBuffer Buffer to read from
	 * @throws BmpFileException Thrown in case the function fails to read the pixel array
	 */
	private void readPixelArray(BufferedInputStream fileBuffer) throws BmpFileException
	{
		try
		{
			// Length of all headers, mandatory and optional
			int expectedPixelArraySize = _fileSize - _pixelArrayOffset;
			_pixelArray = new byte[expectedPixelArraySize];
			
			// Skip to pixel array start
			fileBuffer.skipNBytes(_pixelArrayOffset - HEADER_LEN);
			if(expectedPixelArraySize != fileBuffer.read(_pixelArray,
														 0,
														 expectedPixelArraySize))
			{
				_pixelArray = null;
				throw new BufferUnderflowException();
			} /* if */
		}
		catch (BufferUnderflowException e)
		{
			throw new BmpFileException("Not enough bytes to read the pixel array");
		}
		catch (IOException e)
		{
			throw new BmpFileException(e.getMessage());
		} /* try */
	}
}
