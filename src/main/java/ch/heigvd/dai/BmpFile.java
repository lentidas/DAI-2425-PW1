/**
 * @brief Provides a class that is able to parse a BMP file and split it into more interesting data
 * @class DAI
 * @pw 1
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
	private static final int MAGIC_BYTES_LEN = 2;
	private static final int HEADER_FIELDS_LEN = 4;
	private static final int HEADER_LEN = MAGIC_BYTES_LEN + HEADER_FIELDS_LEN * 3;
	private boolean _hasMessage;
	private byte[] _pixelArray;
	private int _fileSize; // BMP files, as per their header, cannot be bigger than 2^32 bytes
	private int _pixelArrayOffset;
	private int _messageLength;

	/**
	 * Parses and splits a bitmap file into useful data
	 * @param bmpFile Path to bitmap file
	 * @throws BmpFileException Error raised in case the bitmap file is not valid
	 * @throws IOException IO error when handling bitmap file
	 */
	public BmpFile(String bmpFile) throws BmpFileException, IOException
	{
		try (FileInputStream bmpInputStream = new FileInputStream(bmpFile);
		     BufferedInputStream bufferedInputStream = new BufferedInputStream(bmpInputStream))
		{
			readBitmapHeader(bufferedInputStream);
			readPixelArray(bufferedInputStream);
		}
	}

	/**
	 * Returns whether this file has a hidden message inside
	 * @return True if a message is present, false if not
	 */
	public boolean hasMessage()
	{
		return _hasMessage;
	}

	/**
	 * Returns the hidden message's length
	 * @return Hidden message's length if one is present, or 0 if no message is hidden
	 */
	public int getMessageLength()
	{
		return _messageLength;
	}

	/**
	 * Returns the pixel array that was read from the BMP file
	 * @implNote Function returns the actual array, and not a copy. This makes it easier to edit the array and save it
	 * @return Pixel array
	 */
	public byte[] getPixelArray()
	{
		return _pixelArray;
	}

	/**
	 * Checks whether the provided magic bytes are valid for a bitmap file
	 * @param magicBytes Buffer containing the magic bytes
	 * @return True if magic is valid, false if not
	 */
	private boolean isValidMagic(byte[] magicBytes)
	{
		boolean validMagic = false;
		for (String acceptedMagic : VALID_MAGICS)
		{
			if (Arrays.equals(acceptedMagic.getBytes(),
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
			if (MAGIC_BYTES_LEN != fileBuffer.read(magicBytes, 0, MAGIC_BYTES_LEN) 
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
			_messageLength = byteBuffer.getInt();
			_hasMessage = _messageLength > 0;

			// Get the offset to start reading the pixel array from
			byteBuffer.rewind();
			byteBuffer.put(fileBuffer.readNBytes(HEADER_FIELDS_LEN));
			byteBuffer.rewind();
			_pixelArrayOffset = byteBuffer.getInt();
		} catch (BufferUnderflowException e)
		{
			throw new BmpFileException("Not enough bytes to read the entire header");
		} catch (IOException e)
		{
			throw new BmpFileException(e.getMessage());
		} /* try */
	}

	/**
	 * Reads the bitmap file's pixel array
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
			if (expectedPixelArraySize != fileBuffer.read(_pixelArray,
					0,
					expectedPixelArraySize))
			{
				_pixelArray = null;
				throw new BufferUnderflowException();
			} /* if */
		} catch (BufferUnderflowException e)
		{
			throw new BmpFileException("Not enough bytes to read the pixel array");
		} catch (IOException e)
		{
			throw new BmpFileException(e.getMessage());
		} /* try */
	}
}
