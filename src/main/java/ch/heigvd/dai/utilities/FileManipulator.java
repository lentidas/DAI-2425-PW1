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
    if (fileSize >= Integer.MAX_VALUE) throw new Exception(); // FIXME Use proper exception

    byte[] bytes = new byte[(int) fileSize];

    try (FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      int pos = 0;
      int readByte;
      while ((readByte = bufferedInputStream.read()) != -1) {
        bytes[pos++] = (byte) readByte;
      }
    }

    return bytes;
  }

  public void writeBytesToFile(byte[] bytes) throws Exception, IOException {
    if (!writeEnabled) throw new Exception(); // FIXME use proper exception

    try (FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
      for (byte writeByte : bytes) {
        bufferedOutputStream.write(writeByte);
      }
      bufferedOutputStream.flush();
    }
  }
}
