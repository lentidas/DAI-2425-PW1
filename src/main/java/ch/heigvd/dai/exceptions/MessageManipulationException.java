/*
 * @brief   [file description]
 * @class   DAI
 * @pw      1
 * @authors Pedro Alves da Silva, Gonçalo Heleno Carvalheiro
 * Copyright (c) 2024
 */

package ch.heigvd.dai.exceptions;

public class MessageManipulationException extends IllegalArgumentException {
  public MessageManipulationException(String s) {
    super(String.format("Exception thrown while manipulating message: %s", s));
  }
}
