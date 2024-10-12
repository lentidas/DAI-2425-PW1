package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "hide",
    description = "Hide the bits of a message/file into a BMP image.")
public class Hide implements Callable<Integer> {
  @CommandLine.ParentCommand private Root parent;

  @CommandLine.Parameters(index = "0", description = "The file with the content to be hidden.")
  private String filenameToHide;

  @CommandLine.Parameters(
      index = "1",
      description = "The filename where the output should be stored.",
      defaultValue = "out.bmp")
  private String filenameOutput;

  @Override
  public Integer call() {
    System.out.println(
        "Hide message from "
            + filenameToHide
            + " in image "
            + parent.getFilenameBmpImage()
            + " to "
            + filenameOutput);

    System.out.println();
    return 0;
  }
}
