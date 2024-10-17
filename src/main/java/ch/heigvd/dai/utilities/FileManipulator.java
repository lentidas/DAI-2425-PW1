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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implements the necessary functions to read/write the bytes of the file to hide/expose.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class FileManipulator {

  private final File file;
  private boolean writeEnabled = false;

  /**
   * Main constructor.
   *
   * @param filename a {@link String} with the path to the file
   * @throws NullPointerException if the {@code filename }argument is {@code null}
   */
  public FileManipulator(String filename) throws NullPointerException {
    file = new File(filename);
  }

  /**
   * Write constructor.
   *
   * <p>Constructor used when the file is opened with write permissions. This is merely a flag we
   * use to make sure the write function cannot be called unless the object explicitly allows it.
   *
   * @param filename a {@link String} with the path to the file
   * @param writeEnabled a {@code boolean} to enable the {@link
   *     FileManipulator#writeBytesToFile(byte[])} function
   * @throws NullPointerException if the {@code filename} argument is {@code null}
   */
  public FileManipulator(String filename, boolean writeEnabled) throws NullPointerException {
    this(filename);
    this.writeEnabled = writeEnabled;
  }

  /**
   * Reads the file linked to the object and creates a byte array with its content.
   *
   * @return a {@code byte[]} array with the contents of the file
   * @throws IOException if there is an IO error when trying to open the file
   * @throws OutOfMemoryError if the file it too big to fit inside a {@code byte[]} array
   */
  public byte[] readBytesFromFile() throws IOException, OutOfMemoryError {
    long fileSize = this.file.length();

    // A byte array cannot hold more than 2ˆ31 - 1 values.
    if (fileSize >= Integer.MAX_VALUE)
      throw new OutOfMemoryError("The message file is too big for byte[] array.");

    byte[] bytes = new byte[(int) fileSize];

    try (FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      int bytesRead = bufferedInputStream.read(bytes);
      if (bytesRead != fileSize) {
        throw new RuntimeException("Error while reading from the buffer to the byte[] array.");
      }
    }

    return bytes;
  }

  /**
   * Writes a byte array to a file in the given output.
   *
   * <p>Note that this could be a destructive operation if there is already a file at that location,
   * so you should take care to perform the necessary verifications.
   *
   * @param bytes a {@code byte[]} array with the content to write to the file
   * @throws IOException if there is an IO error when trying to open the file
   * @throws RuntimeException if the write operations have not been enabled at the instance creation
   */
  public void writeBytesToFile(byte[] bytes) throws IOException, RuntimeException {
    if (!writeEnabled)
      throw new RuntimeException(
          "Unable to write to file, because write operations are not activated for this object.");

    try (FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
      bufferedOutputStream.write(bytes);
      bufferedOutputStream.flush();
    }
  }
}
