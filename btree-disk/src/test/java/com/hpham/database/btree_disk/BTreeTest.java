package com.hpham.database.btree_disk;

import com.hpham.database.btree_disk.data_types.IntField;
import com.hpham.database.btree_disk.data_types.StringField;
import com.hpham.database.btree_disk.file_formats.Store;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test suite for {@link BTree}.
 * */
public class BTreeTest {
  private BTree<Integer> bTree;
  private static final Integer NUMBER_OF_TEST_RECORDS = 3; // TODO: increase this

  @BeforeEach
  void setup() throws IOException {
    Store.reset();
    bTree = new BTree<>(INT_TYPE_SIGNAL);
  }

  @AfterAll
  static void cleanup() throws IOException {
    Store.INDEX_FILE.deleteAll();
    Store.RECORD_FILE.deleteAll();
  }

  @ParameterizedTest
  @MethodSource("testRecords")
  void testAdd(List<Record<Integer>> records)  {
    AtomicInteger numRecords = new AtomicInteger();
    records.forEach(record -> {
      numRecords.incrementAndGet();
      try {
        bTree.insert(record);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        Record<Integer> read = bTree.findRecord(record.getKey());
        assertThat(read.getKey()).isEqualTo(record.getKey());
        read.getValue().getFields()
            .forEach((k, v) -> assertThat(record.getValue().getField(k)).isEqualTo(v));
        var root = bTree.getRoot();
        assertThat(root.keyTypeSignal).isEqualTo(INT_TYPE_SIGNAL);
        assertThat(root.getParentOffset()).isEqualTo(null);
        assertThat(root.getRecordOffsets()).hasSize(numRecords.get());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

//  @ParameterizedTest
//  @MethodSource("testRecords")
//  void testTreeIntegrityWhenAdd(List<Record<Integer>> testRecords) {
//    testRecords.forEach(record -> {
//      try {
//        bTree.insert(record);
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
//      try {
//        Record<Integer> read = bTree.findRecord(record.getKey());
//        assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
//        assertThat(read.getKey()).isEqualTo(record.getKey());
//        read.getValue().getFields()
//            .forEach((k, v) -> assertThat(record.getValue().getField(k)).isEqualTo(v));
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
//    });

//    checkTreeIntegrity(bTree.getRoot());
//  }

//  @ParameterizedTest
//  @MethodSource("testRecords")
//  void testTreeIntegrityWhenDelete(List<Record<Integer>> testRecords) {
//    testRecords.forEach(record -> bTree.insert(record));
//
//    testRecords.forEach(record -> {
//      bTree.delete(record.getKey());
//      checkTreeIntegrity(bTree.getRoot());
//    });
//  }

//  private <K extends Comparable<K>> void checkTreeIntegrity(BTreeNode<K> root) {
//    if (root == null) {
//      return;
//    }
//    Queue<BTreeNode<K>> queue = new LinkedList<>();
//    queue.offer(root);
//
//    while (!queue.isEmpty()) {
//      // check for ordering, occupancies, and parent-child relationship
//      BTreeNode<K> currentNode = queue.poll();
//      if (currentNode.getIsLeaf()) {
//        var records = currentNode.getRecords();
//        var keys = currentNode.getKeys();
//        assertThat(records.size()).isEqualTo(keys.size());
//        for (int i = 1; i < records.size(); i++) {
//          assertThat(records.get(i).getKey()).isEqualTo(keys.get(i));
//          assertThat(records.get(i).getKey()).isGreaterThan(records.get(i - 1).getKey());
//        }
//        if (!currentNode.isRootNode()) {
//          assertThat(records.size())
//              .isGreaterThanOrEqualTo(FANOUT / 2)
//              .isLessThanOrEqualTo(FANOUT);
//        } else {
//          assertThat(records.size())
//              .isGreaterThanOrEqualTo(1)
//              .isLessThanOrEqualTo(FANOUT);
//        }
//      } else {
//        var keys = currentNode.getKeys();
//        for (int i = 1; i < keys.size(); i++) {
//          assertThat(keys.get(i)).isGreaterThan(keys.get(i - 1));
//        }
//        var pointers = currentNode.getPointers();
//
//        if (currentNode.getParent() == null) {
//          assertThat(pointers.size())
//              .isGreaterThanOrEqualTo(1);
//        } else {
//          assertThat(pointers.size())
//              .isGreaterThanOrEqualTo((int) Math.ceil(((double) FANOUT) / 2));
//        }
//
//        assertThat(pointers.size()).isEqualTo(keys.size() + 1);
//
//        for (var pointer : pointers) {
//          assertThat(pointer.getParent()).isEqualTo(currentNode);
//        }
//      }
//
//      if (!currentNode.getIsLeaf()) {
//        for (var pointer : currentNode.getPointers()) {
//          queue.offer(pointer);
//        }
//      }
//    }
//  }

//  @ParameterizedTest
//  @MethodSource("testRecords")
//  void testDelete(List<Record<Integer>> records) throws RecordNotFoundException {
//    records.forEach(record -> {
//      bTree.insert(record);
//      assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
//    });
//
//    records.forEach(record -> {
//      bTree.delete(record.getKey());
//      assertThat(bTree.findRecord(record.getKey())).isNull();
//    });
//  }

  private static Stream<List<Record<Integer>>> testRecords() {
    return Stream.of(
        generateTestRecordsWithIncrementingKeys(),
        generateTestRecordsWithDecrementingKeys(),
        generateTestRecordsWithRandomizedKeys()
    );
  }

  private static List<Record<Integer>> generateTestRecordsWithIncrementingKeys() {
    return IntStream.range(0, NUMBER_OF_TEST_RECORDS)
        .mapToObj(key ->(Record.<Integer>builder()
            .key(IntField.builder().value(key).build())
            .value(RecordValue.emptyRecordValue()
                .withField(
                    "testField",
                    StringField.fromValue(String.format("%s - %s", key, "testValue"))
                )
            )
            .build()))
        .toList();
  }

  private static List<Record<Integer>> generateTestRecordsWithDecrementingKeys() {
    return IntStream.iterate(NUMBER_OF_TEST_RECORDS, i -> i >= 0, i -> i - 1)
        .mapToObj(key ->(Record.<Integer>builder()
            .key(IntField.builder().value(key).build())
            .value(RecordValue.emptyRecordValue()
                .withField(
                    "testField",
                    StringField.fromValue(String.format("%s - %s", key, "testValue"))
                )
            )
            .build()))
        .toList();
  }

  private static List<Record<Integer>> generateTestRecordsWithRandomizedKeys() {
    Set<Integer> keys = new HashSet<>();
    Random rand = new Random();

    while (keys.size() < NUMBER_OF_TEST_RECORDS) {
      Integer randomKey = rand.nextInt();
      keys.add(randomKey);
    }

    return keys.stream()
        .map(key ->(Record.<Integer>builder()
            .key(IntField.builder().value(key).build())
            .value(RecordValue.emptyRecordValue()
                .withField(
                    "testField",
                    StringField.fromValue(String.format("%s - %s", key, "testValue"))
                )
            ).build()))
        .toList();
  }
//
//  @Test
//  void insertNewRecordWithExistingKey() {
//    bTree.insert(Record.<Integer>builder()
//        .key(IntField.fromValue(1))
//        .value(RecordValue.emptyRecordValue()
//            .withField("field-1", StringField.fromValue("val-1"))
//        )
//        .build()
//    );
//    assertThrows(
//        RecordAlreadyExistException.class,
//        () -> bTree.insert(Record.<Integer>builder()
//            .key(IntField.fromValue(1))
//            .value(RecordValue.emptyRecordValue()
//                .withField("field-2", StringField.fromValue("val-2"))
//            )
//            .build()
//        )
//    );
//  }
//
//  @Test
//  void deleteRecordWithInvalidKey() {
//    bTree.insert(Record.<Integer>builder()
//        .key(IntField.fromValue(1))
//        .value(RecordValue.emptyRecordValue()
//            .withField("field-1", StringField.fromValue("val-1"))
//        )
//        .build()
//    );
//    assertThrows(
//        RecordNotFoundException.class,
//        () -> bTree.delete(IntField.fromValue(2))
//    );
//  }
//
//  @Test
//  void updateRecordWithExistingKey() {
//    bTree.insert(Record.<Integer>builder()
//        .key(IntField.fromValue(1))
//        .value(RecordValue.emptyRecordValue()
//            .withField("field-1", StringField.fromValue("val-1"))
//        )
//        .build()
//    );
//    assertThat(
//        bTree.update(Record.<Integer>builder()
//            .key(IntField.fromValue(1))
//            .value(RecordValue.emptyRecordValue()
//                .withField("field-2", StringField.fromValue("val-2"))
//            )
//            .build()
//        )
//    ).satisfies(record -> {
//      assertThat(record.getKey()).isEqualTo(IntField.fromValue(1));
//      assertThat(record.getValue().getField("field-2"))
//          .isEqualTo(StringField.fromValue("val-2"));
//    });
//
//    assertThat(bTree.findRecord(IntField.fromValue(1))
//        .getValue().getField("field-2"))
//        .isEqualTo(StringField.fromValue("val-2"));
//  }
//
//  @Test
//  void updateInvalidRecord() {
//    bTree.insert(Record.<Integer>builder()
//        .key(IntField.fromValue(1))
//        .value(RecordValue.emptyRecordValue()
//            .withField("field-1", StringField.fromValue("val-1"))
//        )
//        .build());
//    assertThrows(
//        RecordNotFoundException.class,
//        () -> bTree.delete(IntField.fromValue(2))
//    );
//  }
}
