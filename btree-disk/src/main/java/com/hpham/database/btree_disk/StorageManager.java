package com.hpham.database.btree_disk;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class StorageManager {
  public File createFile() throws IOException {
   File file =  new File("index.tc");

    file.createNewFile();

   return file;
  }

  public void writeFile(ByteBuffer bb, File file) {
    Set<OpenOption> options = new HashSet<OpenOption>();
    options.add(APPEND);
    options.add(CREATE);

    // Create the custom permissions attribute.
    Set<PosixFilePermission> perms =
        PosixFilePermissions.fromString("rw-r-----");
    FileAttribute<Set<PosixFilePermission>> attr =
        PosixFilePermissions.asFileAttribute(perms);

    try (SeekableByteChannel sbc =
             Files.newByteChannel(file.toPath(), options, attr)) {
      sbc.write(bb);
    } catch (IOException x) {
      System.out.println("Exception thrown: " + x);
    }
  }

  public static ByteBuffer readFile(Path path) throws IOException {

    final int BUFFER_CAPACITY = 50;
    ByteBuffer buf = ByteBuffer.allocate(BUFFER_CAPACITY);
    // Files.newByteChannel() defaults to StandardOpenOption.READ
    try (SeekableByteChannel sbc = Files.newByteChannel(path)) {


      // Read the bytes with the proper encoding for this platform. If
      // you skip this step, you might see foreign or illegible
      // characters.
      String encoding = System.getProperty("file.encoding");
      while (sbc.read(buf) > 0) {
        buf.flip();
        Charset.forName(encoding).decode(buf);
      }
    }

    return buf;
  }
}
