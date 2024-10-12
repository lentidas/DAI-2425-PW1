package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "expose",
    description = "Expose an hidden file/message from a BMP image.")
public class Expose implements Callable<Integer> {
  @CommandLine.ParentCommand private Root parent;

  @Override
  public Integer call() {
    // TODO Expose message
    System.out.println("Expose message from " + parent.getFilenameBmpImage());

    return 0;
  }
}
