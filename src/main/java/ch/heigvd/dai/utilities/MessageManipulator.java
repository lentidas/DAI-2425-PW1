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

import static ch.heigvd.dai.utilities.BmpFile.*;

import ch.heigvd.dai.exceptions.MessageManipulationException;

/**
 * Implements the logic necessary to hide and expose an array of bytes inside a bitmap image, which
 * is itself parsed using the {@link BmpFile} class.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class MessageManipulator {

  /**
   * Hides a provided message inside the provided data array.
   *
   * <p>Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in data
   * are replaced.
   *
   * @param bmpFile the parsed bitmap file from a {@link BmpFile} instance
   * @param message the message to hide
   * @param bitsPerByte the number of bits to use per message byte
   * @throws MessageManipulationException if one of the arguments is not valid
   */
  public void hideMessage(BmpFile bmpFile, byte[] message, int bitsPerByte)
      throws MessageManipulationException {
    byte[] data = bmpFile.getPixelArray();

    if (bitsPerByte < MIN_BITS_PER_BYTE || bitsPerByte > MAX_BITS_PER_BYTE) {
      throw new MessageManipulationException(
          "Bit count must be between " + MIN_BITS_PER_BYTE + " and " + MAX_BITS_PER_BYTE);
    } /* if */

    if (message.length < MIN_MESSAGE_LENGTH || message.length > MAX_MESSAGE_LENGTH) {
      throw new MessageManipulationException("Invalid message length");
    } /* if */

    // 8 bytes in pixel array to store 1 byte of message
    if (data.length < message.length * MAX_BITS_PER_BYTE) {
      throw new MessageManipulationException("Not enough space to hide message in data");
    } /* if */

    // Mask used to reset the data bits to hide the message in
    byte bit_mask = (byte) ((1 << bitsPerByte) - 1);

    // Bit by bit in message, byte by byte in pixel array
    for (int i = 0; i < message.length * MAX_BITS_PER_BYTE; ++i) {
      int bit_offset = i % MAX_BITS_PER_BYTE;
      int msg_offset = i >> 3;
      byte msg_byte = message[msg_offset];
      byte msg_bit = (byte) ((msg_byte >> (MAX_BITS_PER_BYTE - 1 - bit_offset)) & bit_mask);
      data[i] &= (byte) ~bit_mask;
      data[i] |= msg_bit;
    } /* for */

    bmpFile.setData(data, message.length, bitsPerByte);
  }

  /**
   * Extracts a hidden message from the provided data array.
   *
   * <p>Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in data
   * are set to 0.
   *
   * @param bmpFile the parsed bitmap file from a {@link BmpFile} instance
   * @param message a byte array where the extracted message will be extracted to
   * @throws MessageManipulationException if one of the arguments is not valid
   */
  public void exposeMessage(BmpFile bmpFile, byte[] message) throws MessageManipulationException {
    int bitsPerByte = bmpFile.getBitsPerByte();
    int messageLength = bmpFile.getMessageLength();
    byte[] data = bmpFile.getPixelArray();

    // 8 bytes in pixel array to store 1 byte of message
    if (data.length < message.length * MAX_BITS_PER_BYTE) {
      throw new MessageManipulationException("Data is not enough to retrieve hidden message");
    } /* if */

    // Mask used to reset the data bits to hide the message in
    byte bit_mask = (byte) ((1 << bitsPerByte) - 1);

    // Bit by bit in message, byte by byte in pixel array
    for (int i = 0; i < messageLength * MAX_BITS_PER_BYTE; ++i) {
      int bit_offset = i % MAX_BITS_PER_BYTE;
      int byte_offset = i >> 3;
      byte pixel = data[i];
      byte hidden_bit = (byte) (pixel & bit_mask);

      // Reset data bits
      data[i] = (byte) (pixel ^ hidden_bit);

      // Extract message
      message[byte_offset] |= (byte) (hidden_bit << (MAX_BITS_PER_BYTE - 1 - bit_offset));
    } /* for */

    bmpFile.setData(data, 0, 0);
  }
}
