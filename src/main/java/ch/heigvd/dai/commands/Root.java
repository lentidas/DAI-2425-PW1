package ch.heigvd.dai.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import picocli.CommandLine;

@CommandLine.Command(
    description = "",
    version = "0.0.1",
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

  // TODO Remove the following block if not implemented
  //
  //  @CommandLine.Option(
  //      names = {"-e", "--encryption-key"},
  //      description =
  //          "The GPG encryption key to use to encrypt the message/file before hiding it inside the
  // BMP image.")
  //  protected String encryptionKey;

  public String getFilenameBmpImage() {
    return filenameBmpImage;
  }

  public String getFilenameMessage() {
    return filenameMessage;
  }

  public boolean forceDisabled() {
    return !force;
  }

  public boolean isBmpFileValid() {
    Path path = Paths.get(filenameBmpImage);
    return Files.exists(path) && !Files.isDirectory(path);
  }

  public boolean isMessageFileValid() {
    Path path = Paths.get(filenameMessage);
    return Files.exists(path) && !Files.isDirectory(path);
  }
}
