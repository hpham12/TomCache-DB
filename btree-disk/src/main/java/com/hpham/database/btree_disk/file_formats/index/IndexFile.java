package com.hpham.database.btree_disk.file_formats.index;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import static com.hpham.database.btree_disk.constants.DataConstants.PAGE_SIZE_BYTES;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class IndexFile {
  private File file;
  private SeekableByteChannel byteChannel;

  public void openFile(String fileName) throws IOException {
    file = new File(fileName);
    file.createNewFile();

    Set<OpenOption> options = new HashSet<>();
    options.add(READ);
    options.add(WRITE);
    options.add(CREATE);

    // Create the custom permissions attribute.
    Set<PosixFilePermission> perms =
        PosixFilePermissions.fromString("rw-r-----");
    FileAttribute<Set<PosixFilePermission>> attr =
        PosixFilePermissions.asFileAttribute(perms);

    byteChannel = Files.newByteChannel(file.toPath(), options, attr);
  }

  public ByteBuffer read(long offset) throws IOException {
    long actualPosition = offset * PAGE_SIZE_BYTES;
    byteChannel.position(actualPosition);

    ByteBuffer readBuffer = ByteBuffer.allocateDirect(PAGE_SIZE_BYTES);
    byteChannel.read(readBuffer);

    readBuffer.flip();

    return readBuffer;
  }

  /**
   * TODO: Instead of only appending, have a mechanism to write in empty slots.
   */
  public Long append(ByteBuffer bytes) throws IOException {
    Long newIndexPosition = byteChannel.size();
    byteChannel.position(newIndexPosition);
    byteChannel.write(bytes);

    return newIndexPosition;
  }

  public Long update(ByteBuffer bytes, long offset) throws IOException {
    long actualPosition = offset * PAGE_SIZE_BYTES;
    byteChannel.position(actualPosition);
    byteChannel.write(bytes);

    return actualPosition;
  }

  // TODO: Save empty slots in header/metadata file
  public Long delete(long offset) throws IOException {
    long actualPosition = offset * PAGE_SIZE_BYTES;
    byteChannel.position(actualPosition);
    byteChannel.write(ByteBuffer.wrap(new byte[PAGE_SIZE_BYTES]));

    return actualPosition;
  }

  public void close() throws IOException {
    byteChannel.close();
  }

  public void deleteAll() throws IOException {
    close();
    file.delete();
  }

}
