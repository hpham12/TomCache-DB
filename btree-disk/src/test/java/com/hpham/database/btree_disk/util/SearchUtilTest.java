package com.hpham.database.btree_disk.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link SearchUtil}.
  * */
public class SearchUtilTest {
  private static final List<Integer> allKeys = List.of(1, 3, 5, 7, 9);

  @ParameterizedTest
  @MethodSource("searchForIndexTestCases")
  void searchForIndexTest(TestCase testCase) {
    assertThat(SearchUtil.searchForIndex(testCase.input, allKeys)).isEqualTo(testCase.output);
  }

  @ParameterizedTest
  @MethodSource("searchForIndexOfFirstLargerTestCases")
  void searchForIndexOfFirstLargerTest(TestCase testCase) {
    assertThat(SearchUtil.findFirstLargerIndex(testCase.input, allKeys)).isEqualTo(testCase.output);
  }

  private static Stream<TestCase> searchForIndexTestCases() {
    return Stream.of(
        new TestCase(0, -1),
        new TestCase(5, 2),
        new TestCase(7, 3),
        new TestCase(9, 4)
    );
  }

  private static Stream<TestCase> searchForIndexOfFirstLargerTestCases() {
    return Stream.of(
        new TestCase(0, 0),
        new TestCase(1, 1),
        new TestCase(3, 2),
        new TestCase(4, 2),
        new TestCase(9, 5),
        new TestCase(10, 5)
    );
  }

  private record TestCase(int input, int output) {
  }
}
