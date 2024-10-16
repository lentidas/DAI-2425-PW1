package ch.heigvd.dai.commands;

import ch.heigvd.dai.utilities.BmpFile;
import ch.heigvd.dai.utilities.FileManipulator;
import ch.heigvd.dai.utilities.MessageManipulator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "expose",
    description = "Expose an hidden file/message from a BMP image.")
public class Expose implements Callable<Integer> {
  @CommandLine.ParentCommand private Root parent;

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
