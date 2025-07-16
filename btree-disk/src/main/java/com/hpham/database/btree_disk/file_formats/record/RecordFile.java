package com.hpham.database.btree_disk.file_formats.record;

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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class RecordFile {
  private File file;
  private SeekableByteChannel byteChannel;
  private Boolean isDirty = Boolean.FALSE;
  private Long recordStart;
  private Integer recordSize;

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
    long actualPosition = offset * recordSize;
    byteChannel.position(actualPosition + recordStart);

    ByteBuffer readBuffer = ByteBuffer.allocateDirect(recordSize);
    byteChannel.read(readBuffer);

    readBuffer.flip();

    return readBuffer;
  }

  /**
   * TODO: Instead of only appending, have a mechanism to write in empty slots.
   */
  public Long append(ByteBuffer bytes) throws IOException {
    if (!isDirty) {
      recordSize = bytes.limit();
      // this is the first record, thus it needs to set Record size in the record file header
      RecordFileHeader recordHeader = RecordFileHeader.builder()
          .recordSize(recordSize)
          .build();

      byteChannel.write(recordHeader.serialize());
      recordStart = byteChannel.position();
      isDirty = true;
    }
    Long newIndexPosition = byteChannel.size();
    byteChannel.position(newIndexPosition);
    byteChannel.write(bytes);

    return newIndexPosition;
  }

  public Long update(ByteBuffer bytes, long offset) throws IOException {
    long actualPosition = offset * recordSize + recordStart;
    byteChannel.position(actualPosition);
    byteChannel.write(bytes);

    return actualPosition;
  }

  public void close() throws IOException {
    byteChannel.close();
  }

  public void delete() throws IOException {
    close();
    file.delete();
  }
}
