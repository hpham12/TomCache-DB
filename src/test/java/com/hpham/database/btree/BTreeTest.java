package com.hpham.database.btree;

import static com.hpham.database.btree.BTree.FANOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hpham.database.btree.exceptions.RecordAlreadyExistException;
import com.hpham.database.btree.exceptions.RecordNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test suite for {@link BTree}.
 * */
public class BTreeTest {
  private BTree<Integer> bTree;
  private static final Integer NUMBER_OF_TEST_RECORDS = 10000;

  @BeforeEach
  void setup() {
    bTree = new BTree<>();
  }

  @ParameterizedTest
  @MethodSource("testRecords")
  void testAdd(List<Record<Integer, Object>> records) {
    records.forEach(record -> {
      bTree.insert(record);
      assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
    });
  }

  @ParameterizedTest
  @MethodSource("testRecords")
  void testTreeIntegrityWhenAdd(List<Record<Integer, Object>> testRecords) {
    testRecords.forEach(record -> {
      bTree.insert(record);
      assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
    });

    checkTreeIntegrity(bTree.getRoot());
  }

  @ParameterizedTest
  @MethodSource("testRecords")
  void testTreeIntegrityWhenDelete(List<Record<Integer, Object>> testRecords) {
    testRecords.forEach(record -> bTree.insert(record));

    testRecords.forEach(record -> {
      bTree.delete(record.getKey());
      checkTreeIntegrity(bTree.getRoot());
    });
  }

  private <K extends Comparable<K>> void checkTreeIntegrity(BTreeNode<K> root) {
    if (root == null) {
      return;
    }
    Queue<BTreeNode<K>> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
      // check for ordering, occupancies, and parent-child relationship
      BTreeNode<K> currentNode = queue.poll();
      if (currentNode.getIsLeaf()) {
        var records = currentNode.getRecords();
        var keys = currentNode.getKeys();
        assertThat(records.size()).isEqualTo(keys.size());
        for (int i = 1; i < records.size(); i++) {
          assertThat(records.get(i).getKey()).isEqualTo(keys.get(i));
          assertThat(records.get(i).getKey()).isGreaterThan(records.get(i - 1).getKey());
        }
        if (!currentNode.isRootNode()) {
          assertThat(records.size())
              .isGreaterThanOrEqualTo(FANOUT / 2)
              .isLessThanOrEqualTo(FANOUT);
        } else {
          assertThat(records.size())
              .isGreaterThanOrEqualTo(1)
              .isLessThanOrEqualTo(FANOUT);
        }
      } else {
        var keys = currentNode.getKeys();
        for (int i = 1; i < keys.size(); i++) {
          assertThat(keys.get(i)).isGreaterThan(keys.get(i - 1));
        }
        var pointers = currentNode.getPointers();

        if (currentNode.getParent() == null) {
          assertThat(pointers.size())
              .isGreaterThanOrEqualTo(1);
        } else {
          assertThat(pointers.size())
              .isGreaterThanOrEqualTo((int) Math.ceil(((double) FANOUT) / 2));
        }

        assertThat(pointers.size()).isEqualTo(keys.size() + 1);

        for (var pointer : pointers) {
          assertThat(pointer.getParent()).isEqualTo(currentNode);
        }
      }

      if (!currentNode.getIsLeaf()) {
        for (var pointer : currentNode.getPointers()) {
          queue.offer(pointer);
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("testRecords")
  void testDelete(List<Record<Integer, Object>> records) throws RecordNotFoundException {
    records.forEach(record -> {
      bTree.insert(record);
      assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
    });

    records.forEach(record -> {
      bTree.delete(record.getKey());
      assertThat(bTree.findRecord(record.getKey())).isNull();
    });
  }

  private static Stream<List<Record<Integer, String>>> testRecords() {
    return Stream.of(
        generateTestRecordsWithIncrementingKeys(),
        generateTestRecordsWithDecrementingKeys(),
        generateTestRecordsWithRandomizedKeys()
    );
  }

  private static List<Record<Integer, String>> generateTestRecordsWithIncrementingKeys() {
    return IntStream.range(0, NUMBER_OF_TEST_RECORDS)
        .mapToObj(key -> Record.<Integer, String>builder()
            .key(key)
            .value(String.format("%s - %s", key, "testValue"))
            .build())
        .toList();
  }

  private static List<Record<Integer, String>> generateTestRecordsWithDecrementingKeys() {
    return IntStream.iterate(NUMBER_OF_TEST_RECORDS, i -> i >= 0, i -> i - 1)
        .mapToObj(key -> Record.<Integer, String>builder()
            .key(key)
            .value(String.format("%s - %s", key, "testValue"))
            .build())
        .toList();
  }

  private static List<Record<Integer, String>> generateTestRecordsWithRandomizedKeys() {
    Set<Integer> keys = new HashSet<>();
    Random rand = new Random();

    while (keys.size() < NUMBER_OF_TEST_RECORDS) {
      Integer randomKey = rand.nextInt();
      keys.add(randomKey);
    }

    return keys.stream()
        .map(key -> Record.<Integer, String>builder()
            .key(key)
            .value(String.format("%s - %s", key, "testValue"))
            .build())
        .toList();
  }

  @Test
  void insertNewRecordWithExistingKey() {
    bTree.insert(Record.<Integer, Object>builder().key(1).value("val-1").build());
    assertThrows(
        RecordAlreadyExistException.class,
        () -> bTree.insert(Record.<Integer, Object>builder().key(1).value("val-2").build())
    );
  }

  @Test
  void deleteRecordWithInvalidKey() {
    bTree.insert(Record.<Integer, Object>builder().key(1).value("val-1").build());
    assertThrows(
        RecordNotFoundException.class,
        () -> bTree.delete(2)
    );
  }

  @Test
  void updateRecordWithExistingKey() {
    bTree.insert(Record.<Integer, Object>builder().key(1).value("val-1").build());
    assertThat(
        bTree.update(Record.<Integer, Object>builder().key(1).value("val-2").build())
    ).satisfies(record -> {
      assertThat(record.getKey()).isEqualTo(1);
      assertThat(record.getValue()).isEqualTo("val-2");
    });

    assertThat(bTree.findRecord(1).getValue()).isEqualTo("val-2");
  }

  @Test
  void updateInvalidRecord() {
    bTree.insert(Record.<Integer, Object>builder().key(1).value("val-1").build());
    assertThrows(
        RecordNotFoundException.class,
        () -> bTree.delete(2)
    );
  }
}
