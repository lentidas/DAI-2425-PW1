package ch.heigvd.dai.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManipulator {

  private final File file;

  private boolean writeEnabled = false;

  public FileManipulator(String filename) throws NullPointerException {
    file = new File(filename);
  }

  public FileManipulator(String filename, boolean writeEnabled) throws NullPointerException {
    this(filename);
    this.writeEnabled = writeEnabled;
  }

  public byte[] readBytesFromFile() throws IOException, Exception {
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

  public void writeBytesToFile(byte[] bytes) throws Exception, IOException {
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
