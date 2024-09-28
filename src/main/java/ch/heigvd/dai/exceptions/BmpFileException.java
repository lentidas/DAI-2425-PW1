/**
 * @brief   Exception raised when the BMP file's sanity checks fail
 * @class   DAI
 * @pw      1
 * @authors Pedro Alves da Silva, Gonçalo Heleno Carvalheiro
 * Copyright (c) 2024
 */

package ch.heigvd.dai.exceptions;

public class BmpFileException extends IllegalArgumentException
{
	public BmpFileException(String s)
	{
		super(String.format("BMP file parsing error: %s", s));
	}
}
