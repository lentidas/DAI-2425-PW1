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
    name = "expose",
    description = "Expose an hidden file/message from a BMP image.")
public class Expose implements Callable<Integer> {

  @CommandLine.ParentCommand private Root parent;

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
    if (Files.exists(Paths.get(parent.getFilenameMessage()))) {
      if (Files.isDirectory(Paths.get(parent.getFilenameMessage()))) {
        System.err.println(
            "The path for the message output is a directory.\n"
                + "Please provide a different path.");
        return 1;
      }
      if (parent.forceDisabled()) {
        System.err.println(
            "A file already exists on the output path provided.\n"
                + "If you are sure you want to overwrite that file, enable the '--force' flag.");
        return 1;
      }
    }

    if (!parent.isBmpFileValid()) {
      System.err.println(
          "BMP file provided either does not exist or is a directory.\n"
              + "Please provide a path to a valid BMP file.");
      return 1;
    }

    try {
      BmpFile bmpFile = new BmpFile(parent.getFilenameBmpImage());

      if (!bmpFile.hasMessage()) {
        System.err.println(
            "BMP file provided does not have a message hidden inside.\n"
                + "Please provide a path to a BMP file containing a message.");
        return 1;
      }

      byte[] message = new byte[bmpFile.getMessageLength()];
      FileManipulator fileManipulator = new FileManipulator(parent.getFilenameMessage(), true);
      MessageManipulator messageManipulator = new MessageManipulator();

      messageManipulator.exposeMessage(bmpFile, message);
      fileManipulator.writeBytesToFile(message);
    } catch (Exception e) {
      System.err.println("Error while hiding message! Exception message:\n" + e.getMessage());
      return 1;
    }

    return 0;
  }
}
