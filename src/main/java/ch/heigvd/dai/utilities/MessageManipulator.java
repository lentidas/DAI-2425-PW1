/*
 * @brief   Class provides functions to manipulate messages, whether it means hiding or exposing it
 * @class   DAI
 * @pw      1
 * @authors Pedro Alves da Silva, Gonçalo Heleno Carvalheiro
 * Copyright (c) 2024
 */

package ch.heigvd.dai.utilities;

import static ch.heigvd.dai.utilities.BmpFile.*;

import ch.heigvd.dai.exceptions.MessageManipulationException;

public class MessageManipulator {
  /**
   * Hides a provided message inside the provided data array
   *
   * @param bmpFile Parsed bitmap file instance
   * @param message Message to hide
   * @param bitsPerByte Number of bits to use per message byte
   * @throws MessageManipulationException Raised in case one of the arguments is not valid
   * @implNote Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in
   *     data are replaced
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
   * Extracts a hidden message from the provided data array
   *
   * @param bmpFile Parsed bitmap file instance
   * @param message Message extracted
   * @throws MessageManipulationException Raised in case one of the arguments is not valid
   * @implNote Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in
   *     data are set to 0
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
