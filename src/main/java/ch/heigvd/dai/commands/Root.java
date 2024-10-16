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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import picocli.CommandLine;

/**
 * Implements the root command for executing the program on a CLI.
 *
 * <p>This class defines the parameters and options that are common throughout the {@link Hide} and
 * {@link Expose} subcommands. For checking the usage of the command, use the {@code --help} option.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
@CommandLine.Command(
    description = "shadow - a CLI tool to hide files inside BMP images",
    version = "0.0.1", // x-release-please-version
    subcommands = {
      Hide.class,
      Expose.class,
    },
    scope = CommandLine.ScopeType.INHERIT,
    mixinStandardHelpOptions = true)
public class Root {

  @CommandLine.Parameters(
      index = "0",
      description =
          "The BMP image where the content will be hidden or where to read the content from.")
  private String filenameBmpImage;

  @CommandLine.Parameters(
      index = "1",
      description =
          "The file where to read the message from or the file destination where to write the message to, depending on the operation mode.")
  private String filenameMessage;

  @CommandLine.Option(
      names = {"-f", "--force"},
      description = "Overwrite output file if something already exists in the path.")
  private boolean force;

  // TODO Potentially implement encryption in the future.
  //
  //  @CommandLine.Option(
  //      names = {"-e", "--encryption-key"},
  //      description =
  //          "The GPG encryption key to use to encrypt the message/file before hiding it inside the
  // BMP image.")
  //  protected String encryptionKey;

  /**
   * Getter for the subcommands to read the path to the bitmap image passed as an argument.
   *
   * @return a {@link String} with the path passed as an argument
   */
  public String getFilenameBmpImage() {
    return filenameBmpImage;
  }

  /**
   * Getter for the subcommands to read the path to the message file to be hidden passed as an
   * argument.
   *
   * @return a {@link String} with the path passed as an argument
   */
  public String getFilenameMessage() {
    return filenameMessage;
  }

  /**
   * Getter to check if the {@code --force} flag has been enabled.
   *
   * @return {@code true} if {@code --force} is disabled, {@code false} if enabled
   */
  public boolean forceDisabled() {
    return !force;
  }

  /**
   * Checks if the path for the bitmap image is for a valid file and if it exists.
   *
   * @return {@code true} if the file is valid, {@code false} if not
   */
  public boolean isBmpFileValid() {
    Path path = Paths.get(filenameBmpImage);
    return Files.exists(path) && !Files.isDirectory(path);
  }

  /**
   * Checks if the path for the message file is for a valid file and if it exists.
   *
   * @return {@code true} if the file is valid, {@code false} if not
   */
  public boolean isMessageFileValid() {
    Path path = Paths.get(filenameMessage);
    return Files.exists(path) && !Files.isDirectory(path);
  }
}
