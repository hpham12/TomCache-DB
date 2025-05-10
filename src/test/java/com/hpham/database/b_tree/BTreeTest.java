package com.hpham.database.b_tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BTreeTest {
    private BTree<Integer> bTree;

    @BeforeEach
    void setup() {
        bTree = new BTree<>();
    }

    @Test
    void testAdd() throws IllegalAccessException{
        for (int i = 0; i < 3; i++) {
            Record<Integer, ?> entry = new Record<>(i, "Hello");
            bTree.insert(entry);
            assertThat(bTree.findRecord(i)).isEqualTo(entry);
        }
    }
}
