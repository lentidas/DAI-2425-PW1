/*
 * shadow - a CLI tool to hide files inside BMP images
 * Copyright (C) 2024 Pedro Alves da Silva, Gonçalo Carvalheiro Heleno
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.heigvd.dai.utilities;

import ch.heigvd.dai.exceptions.BmpFileException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Implements the required logic to parse a bitmap file and modify its content.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class BmpFile {

  private static final String[] VALID_MAGICS = {"BM", "BA", "CI", "CP", "IC", "PT"};
  public static final int MIN_MESSAGE_LENGTH = 1;
  public static final int MAX_MESSAGE_LENGTH =
      0x1FFFFFFF; // 29 bits. Last 3 bits are for bits-per-byte
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
   * Parses and splits a bitmap file into useful data.
   *
   * @param bmpFilePath a {@link String} with the path to a bitmap file
   * @throws BmpFileException if the bitmap file is not valid
   * @throws IOException if there is an IO error when handling the bitmap file
   */
  public BmpFile(String bmpFilePath) throws BmpFileException, IOException {
    try (FileInputStream bmpInputStream = new FileInputStream(bmpFilePath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(bmpInputStream)) {
      readBitmapHeader(bufferedInputStream);
      readPixelArray(bufferedInputStream);
      _bmpFilePath = bmpFilePath;
    }
  }

  /**
   * Returns whether this file has a hidden message inside.
   *
   * @return {@code true} if a message is present, {@code false} if not
   */
  public boolean hasMessage() {
    return _hasMessage;
  }

  /**
   * Returns the hidden message's length.
   *
   * @return Hidden message's length if one is present, or 0 if no message is hidden
   */
  public int getMessageLength() {
    return _messageLength;
  }

  /**
   * Returns the number of bits belonging to the hidden message per data byte.
   *
   * @return an {@code int} with the number of bits per data byte
   */
  public int getBitsPerByte() {
    return _bitsPerByte;
  }

  /**
   * Changes the pixel array data, hidden message length, and number of bits per data byte.
   *
   * @param data new pixel array as a {@code byte[]}
   * @param messageLength new message length
   * @param bitsPerByte number of bits per data byte
   */
  public void setData(byte[] data, int messageLength, int bitsPerByte) {
    if (data.length != _pixelArray.length) {
      throw new BmpFileException("Incoherent data length");
    }

    if (messageLength > data.length) {
      throw new BmpFileException("Incoherent message length");
    }

    // We only accept multiples of 2
    if (bitsPerByte != 1 && bitsPerByte % 2 != 0) {
      throw new BmpFileException("Invalid bits per byte");
    }

    _pixelArray = data.clone();
    _bitsPerByte = bitsPerByte;
    _messageLength = messageLength;
  }

  /**
   * Returns the pixel array that was read from the BMP file.
   *
   * <p>This function returns a copy of the pixel array of the instance.
   *
   * @return a {@code byte[]} with the pixel array
   */
  public byte[] getPixelArray() {
    return _pixelArray.clone();
  }

  /**
   * Dumps the pixel array into the provided output file.
   *
   * <p>Note that the original input file must still exist.
   *
   * @param outputFilePath a {@link String} with the path to the output file which must not be the
   *     same file as the input file
   * @throws IOException if an IO error occurs with either the input or output file
   */
  public void saveFile(String outputFilePath) throws IOException {
    try (FileInputStream bmpInputStream = new FileInputStream(_bmpFilePath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(bmpInputStream);
        FileOutputStream bmpOutputStream = new FileOutputStream(outputFilePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(bmpOutputStream)) {
      // Write unaltered header (magic bytes + file size)
      bufferedOutputStream.write(
          bufferedInputStream.readNBytes(MAGIC_BYTES_LEN + HEADER_FIELDS_LEN));

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
      bufferedOutputStream.write(
          bufferedInputStream.readNBytes(_fileSize - _pixelArray.length - HEADER_LEN));

      // Write full pixel array
      bufferedOutputStream.write(_pixelArray);
      bufferedOutputStream.flush();
    }
  }

  /**
   * Checks whether the provided magic bytes are valid for a bitmap file.
   *
   * @param magicBytes a {@code byte[]} containing the magic bytes
   * @return {@code true} if magic is valid, {@code false} if not
   */
  private boolean isValidMagic(byte[] magicBytes) {
    boolean validMagic = false;
    for (String acceptedMagic : VALID_MAGICS) {
      if (Arrays.equals(
          acceptedMagic.getBytes(), 0, MAGIC_BYTES_LEN, magicBytes, 0, MAGIC_BYTES_LEN)) {
        validMagic = true;
        break;
      } /* if */
    } /* for */

    return validMagic;
  }

  /**
   * Reads and checks the file's header.
   *
   * @param fileBuffer a {@link BufferedInputStream} to read from
   * @throws BmpFileException if the function fails to read the file's header
   */
  private void readBitmapHeader(BufferedInputStream fileBuffer) throws BmpFileException {
    byte[] magicBytes = new byte[MAGIC_BYTES_LEN];
    ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_FIELDS_LEN);

    // Bitmap headers are little endian
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

    try {
      // Check magic bytes
      if (MAGIC_BYTES_LEN != fileBuffer.read(magicBytes, 0, MAGIC_BYTES_LEN)
          || !isValidMagic(magicBytes)) {
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
      if (_bitsPerByte < 0) {
        _bitsPerByte *= -1;
      }
      _bitsPerByte += 1; // Compensate for the fact we have a range from 1 to 8
      _hasMessage = _messageLength > 0;

      // Get the offset to start reading the pixel array from
      byteBuffer.rewind();
      byteBuffer.put(fileBuffer.readNBytes(HEADER_FIELDS_LEN));
      byteBuffer.rewind();
      _pixelArrayOffset = byteBuffer.getInt();
    } catch (BufferUnderflowException e) {
      throw new BmpFileException("Not enough bytes to read the entire header");
    } catch (IOException e) {
      throw new BmpFileException(e.getMessage());
    } /* try */
  }

  /**
   * Reads the bitmap file's pixel array.
   *
   * @param fileBuffer a {@link BufferedInputStream} to read from
   * @throws BmpFileException if the function fails to read the pixel array
   */
  private void readPixelArray(BufferedInputStream fileBuffer) throws BmpFileException {
    try {
      // Length of all headers, mandatory and optional
      int expectedPixelArraySize = _fileSize - _pixelArrayOffset;
      _pixelArray = new byte[expectedPixelArraySize];

      // Skip to pixel array start
      fileBuffer.skipNBytes(_pixelArrayOffset - HEADER_LEN);
      if (expectedPixelArraySize != fileBuffer.read(_pixelArray, 0, expectedPixelArraySize)) {
        _pixelArray = null;
        throw new BufferUnderflowException();
      } /* if */
    } catch (BufferUnderflowException e) {
      throw new BmpFileException("Not enough bytes to read the pixel array");
    } catch (IOException e) {
      throw new BmpFileException(e.getMessage());
    } /* try */
  }
}
