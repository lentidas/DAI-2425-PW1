/*
 * @brief   Class provides functions to manipulate messages, whether it means hiding or exposing it
 * @class   DAI
 * @pw      1
 * @authors Pedro Alves da Silva, Gonçalo Heleno Carvalheiro
 * Copyright (c) 2024
 */

package ch.heigvd.dai;

import ch.heigvd.dai.exceptions.MessageManipulationException;

public class MessageManipulator
{
	private static final int MIN_MESSAGE_LENGTH = 1;
	private static final int MAX_MESSAGE_LENGTH = 0x1FFFFFFF; // 29 bits. Last 3 bits are for bits-per-byte
	private static final int MIN_BITS_PER_BYTE = 1;
	private static final int MAX_BITS_PER_BYTE = 8;

	/**
	 * Hides a provided message inside the provided data array
	 *
	 * @param data        Data used to hide message in
	 * @param message     Message to hide
	 * @param bitsPerByte Number of bits to use per message byte
	 * @throws MessageManipulationException Raised in case one of the arguments is not valid
	 * @implNote Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in data are replaced
	 */
	public void hideMessage(byte[] data, byte[] message, int bitsPerByte) throws MessageManipulationException
	{
		if (bitsPerByte < MIN_BITS_PER_BYTE || bitsPerByte > MAX_BITS_PER_BYTE)
		{
			throw new MessageManipulationException("Bit count must be between " + MIN_BITS_PER_BYTE + " and " + MAX_BITS_PER_BYTE);
		} /* if */

		if (message.length < MIN_MESSAGE_LENGTH || message.length > MAX_MESSAGE_LENGTH)
		{
			throw new MessageManipulationException("Invalid message length");
		} /* if */

		// Using Rsh allows us to understand effectively how many bytes are needed to store the message
		if (data.length < (message.length >> (bitsPerByte - 1)))
		{
			throw new MessageManipulationException("Not enough space to hide message in data");
		} /* if */

		// Mask used to reset the data bits to hide the message in
		byte bit_mask = (byte) ((1 << bitsPerByte) - 1);

		for (int i = 0; i < data.length; ++i)
		{
			int bit_offset = i % MAX_BITS_PER_BYTE;
			int msg_offset = i >> 3;
			byte msg_byte = message[msg_offset];
			byte msg_bit = (byte) ((msg_byte >> (MAX_BITS_PER_BYTE - 1 - bit_offset)) & bit_mask);
			data[i] &= (byte) ~bit_mask;
			data[i] |= msg_bit;
		} /* for */
	}

	/**
	 * Extracts a hidden message from the provided data array
	 *
	 * @param data        Data used to extract the message from
	 * @param message     Message extracted
	 * @param bitsPerByte Number of bits to use per message byte
	 * @throws MessageManipulationException Raised in case one of the arguments is not valid
	 * @implNote Data array is used as input and output. Each of the (bitsPerByte) LSb of each byte in data are set to 0
	 */
	public void exposeMessage(byte[] data, byte[] message, int messageLength, int bitsPerByte) throws MessageManipulationException
	{
		if (bitsPerByte < MIN_BITS_PER_BYTE || bitsPerByte > MAX_BITS_PER_BYTE)
		{
			throw new MessageManipulationException("Bit count must be between " + MIN_BITS_PER_BYTE + " and " + MAX_BITS_PER_BYTE);
		} /* if */

		if (messageLength < MIN_MESSAGE_LENGTH || messageLength > MAX_MESSAGE_LENGTH)
		{
			throw new MessageManipulationException("Invalid message length");
		} /* if */

		// Using Rsh allows us to understand effectively how many bytes are needed to store the message
		if (data.length < (messageLength >> (bitsPerByte - 1)))
		{
			throw new MessageManipulationException("Data is not enough to retrieve hidden message");
		} /* if */

		// Mask used to reset the data bits to hide the message in
		byte bit_mask = (byte) ((1 << bitsPerByte) - 1);

		for (int i = 0; i < data.length; ++i)
		{
			int bit_offset = i % MAX_BITS_PER_BYTE;
			int msg_offset = i >> 3;
			byte pixel = data[i];
			byte hidden_bit = (byte) (pixel & bit_mask);

			// Reset data bits
			data[i] = (byte) (pixel ^ hidden_bit);

			// Extract message
			message[msg_offset] |= (byte) (hidden_bit << bit_offset);
		} /* for */
	}
}
