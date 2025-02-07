package com.varlanv.testnameconvention;

import com.varlanv.testnameconvention.commontest.UnitTest;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MethodNameFromDisplayNameTest implements UnitTest {

    @TestFactory
    Stream<DynamicTest> defaultDataTable() {
        return Stream.of(
                Map.entry("", ""),
                Map.entry("   ", ""),
                Map.entry("1", ""),
                Map.entry("12345", ""),
                Map.entry("_123_", ""),
                Map.entry("___", ""),
                Map.entry("Some good test name", "some_good_test_name"),
                Map.entry("Some good test name, which has comas, dots., question? marks, and exclamation marks!", "some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks"),
                Map.entry("Some123good456test789name0", "some123good456test789name0"),
                Map.entry("Слава Україні", ""),
                Map.entry("qwe", "qwe"),
                Map.entry("___abc___", "abc"),
                Map.entry("a_b_c_", "a_b_c"),
                Map.entry("a_b_c", "a_b_c"),
                Map.entry("-a_b_c-", "a_b_c"),
                Map.entry("a_b_c-", "a_b_c"),
                Map.entry("-a_b_c", "a_b_c"),
                Map.entry("Some good\n multiline   test\n name, which also has multiple whitespaces and_underscores   ", "some_good_multiline_test_name_which_also_has_multiple_whitespaces_and_underscores")
            )
            .map(entry -> DynamicTest.dynamicTest(
                    "Display name \"%s\" should be converted to method name \"%s\"".formatted(entry.getKey(), entry.getValue()),
                    () -> {
                        var expectedMethodName = entry.getValue();
                        var displayName = entry.getKey();
                        var actualMethodName = new MethodNameFromDisplayName(
                            displayName,
                            "anyOriginalMethodName"
                        ).newName();
                        assertThat(actualMethodName).isEqualTo(expectedMethodName);
                    }
                )
            );
    }
}
