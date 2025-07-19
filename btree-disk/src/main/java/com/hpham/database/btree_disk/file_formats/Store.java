package com.hpham.database.btree_disk.file_formats;

import com.hpham.database.btree_disk.file_formats.index.IndexFile;
import com.hpham.database.btree_disk.file_formats.record.RecordFile;

import java.io.IOException;

public class Store {
  public static IndexFile INDEX_FILE;
  public static RecordFile RECORD_FILE;

  static {
    try {
      INDEX_FILE = new IndexFile("index.tc");
      RECORD_FILE = new RecordFile("record.tc");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void reset() throws IOException {
    INDEX_FILE.deleteAll();
    RECORD_FILE.deleteAll();
    try {
      INDEX_FILE = new IndexFile("index.tc");
      RECORD_FILE = new RecordFile("record.tc");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
