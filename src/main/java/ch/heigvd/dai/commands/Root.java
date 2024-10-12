package ch.heigvd.dai.commands;

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

  //  @CommandLine.Option(
  //      names = {"-e", "--encryption-key"},
  //      description =
  //          "The GPG encryption key to use to encrypt the message/file before hiding it inside the
  // BMP image.")
  //  protected String encryptionKey;

  public String getFilenameBmpImage() {
    return filenameBmpImage;
  }
}
