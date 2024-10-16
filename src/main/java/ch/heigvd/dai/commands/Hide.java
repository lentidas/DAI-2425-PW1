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

package ch.heigvd.dai.commands;

import ch.heigvd.dai.utilities.BmpFile;
import ch.heigvd.dai.utilities.FileManipulator;
import ch.heigvd.dai.utilities.MessageManipulator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Implements the {@code hide} subcommand for executing the program on a CLI.
 *
 * <p>This class defines the parameters and options that are only specific for this subcommand. For
 * checking the usage of the subcommand, use the {@code --help} option.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
@CommandLine.Command(
    name = "hide",
    description = "Hide the bits of a message/file into a BMP image.")
public class Hide implements Callable<Integer> {

  @CommandLine.ParentCommand private Root parent;

  @CommandLine.Parameters(
      index = "0",
      description =
          "The filename where the image with the hidden content will be stored. Defaults to './output.bmp'.",
      defaultValue = "output.bmp")
  private String filenameOutput;

  @CommandLine.Option(
      names = {"-b", "--bits-per-byte"},
      description =
          "Number of bits to hide per byte of information of the BMP image. Note that the original bits of the image will be overwritten and it may be more noticeable that something is hidden within the image.",
      defaultValue = "1")
  private int bitsPerByte;

  /**
   * Call function that contains the logic of the subcommand.
   *
   * <p>This function performs the multiple input validation checks and outputs an exit code
   * accordingly. An error message is displayed to explain why the command failed.
   *
   * @return exit code 1 if there was an error, 0 otherwise
   */
  @Override
  public Integer call() {
    if (!isPowerOfTwo(bitsPerByte) || bitsPerByte < 1 || bitsPerByte > 8) {
      System.err.println("--bits-per-byte needs to be a power of 2 between 1 and 8 inclusively");
      return 1;
    }

    if (parent.forceDisabled() && Files.exists(Paths.get(filenameOutput))) {
      System.err.println(
          "A file already exists on the output path provided.\n"
              + "If you are sure you want to overwrite it, enable the '--force' flag.");
      return 1;
    }

    if (!parent.isMessageFileValid()) {
      System.err.println(
          "Message file either does not exist or is a directory.\n"
              + "Please provide a path to a valid file.");
      return 1;
    }

    if (!parent.isBmpFileValid()) {
      System.err.println(
          "BMP file provided either does not exist or is a directory.\n"
              + "Please provide a path to a valid BMP file.");
      return 1;
    }

    try {
      BmpFile bmpFile = new BmpFile(parent.getFilenameBmpImage());

      if (parent.forceDisabled() && bmpFile.hasMessage()) {
        System.err.println(
            "BMP file provided already has a message hidden inside.\n"
                + "If you are sure you want to overwrite it, enable the '--force' flag.");
        return 1;
      }

      FileManipulator fileManipulator = new FileManipulator(parent.getFilenameMessage());
      MessageManipulator messageManipulator = new MessageManipulator();

      messageManipulator.hideMessage(bmpFile, fileManipulator.readBytesFromFile(), bitsPerByte);
      bmpFile.saveFile(filenameOutput);
    } catch (Exception e) {
      System.err.println("Error while hiding message! Exception message:\n" + e.getMessage());
      return 1;
    }

    return 0;
  }

  // Ref: https://www.baeldung.com/java-check-number-power-of-two
  private boolean isPowerOfTwo(int n) {
    return (n != 0) && ((n & (n - 1)) == 0);
  }
}
