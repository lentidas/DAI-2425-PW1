package ch.heigvd.dai;

import ch.heigvd.dai.commands.Root;
import java.io.File;
import picocli.CommandLine;

public class Main {
  public static void main(String[] args) {
    // Example from class -
    // https://github.com/heig-vd-dai-course/heig-vd-dai-course-java-ios-practical-content-template/blob/778e1934a64f338e93613afbb31dd9e92356d7c4/src/main/java/ch/heigvd/dai/Main.java#L10
    // Define command name - source: https://stackoverflow.com/a/11159435
    String jarFilename =
        new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getName();

    // Create root command and CommandLine
    CommandLine command = new CommandLine(new Root());
    command.setCommandName(jarFilename).setCaseInsensitiveEnumValuesAllowed(true);

    // Calculate execution time for root command and its subcommands
    Long start = System.nanoTime();
    int exitCode = command.execute(args);
    Long end = System.nanoTime();

    if (exitCode == 0) {
      System.out.println("Execution time: " + (end - start) / (1000 * 1000) + " ms");
    }

    System.exit(exitCode);
  }
}
