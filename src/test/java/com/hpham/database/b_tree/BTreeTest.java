package com.hpham.database.b_tree;

import com.hpham.database.b_tree.exceptions.RecordNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hpham.database.b_tree.BTree.FANOUT;
import static org.assertj.core.api.Assertions.assertThat;

public class BTreeTest {
    private BTree<Integer> bTree;
    private static final Integer NUMBER_OF_TEST_RECORDS = 10000;

    @BeforeEach
    void setup() {
        bTree = new BTree<>();
    }

    @ParameterizedTest
    @MethodSource("testRecords")
    void testAdd(List<Record<Integer, String>> records) {
        records.forEach(record -> {
            bTree.insert(record);
            assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
        });
    }

    @ParameterizedTest
    @MethodSource("testRecords")
    void testTreeIntegrityWhenAdd(List<Record<Integer, String>> testRecords) {
        testRecords.forEach(record -> {
            bTree.insert(record);
            assertThat(bTree.findRecord(record.getKey())).isEqualTo(record);
        });

        checkTreeIntegrity(bTree.getRoot());
    }

    @ParameterizedTest
    @MethodSource("testRecords")
    void testTreeIntegrityWhenDelete(List<Record<Integer, String>> testRecords) {
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
                for (int i = 1; i < records.size(); i++) {
                    assertThat(records.get(i).getKey()).isGreaterThan(records.get(i - 1).getKey());
                }
                if (!currentNode.isRootNode()) {
                    assertThat(records.size())
                            .isGreaterThanOrEqualTo(FANOUT/2)
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
                            .isGreaterThanOrEqualTo((int) Math.ceil(((double) FANOUT)/2));
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
    void testDelete(List<Record<Integer, String>> records) throws RecordNotFoundException {
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
                .mapToObj(key -> Record.<Integer, String> builder()
                        .key(key)
                        .value(String.format("%s - %s", key, "testValue"))
                        .build())
                .toList();
    }

    private static List<Record<Integer, String>> generateTestRecordsWithDecrementingKeys() {
        return IntStream.iterate(NUMBER_OF_TEST_RECORDS, i -> i >= 0, i -> i - 1)
                .mapToObj(key -> Record.<Integer, String> builder()
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
            if (!keys.contains(randomKey)) {
                keys.add(randomKey);
            }
        }

        return keys.stream()
                .map(key -> Record.<Integer, String> builder()
                        .key(key)
                        .value(String.format("%s - %s", key, "testValue"))
                        .build())
                .toList();
    }
}
