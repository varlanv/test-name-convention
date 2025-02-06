package com.varlanv.testnameconvention.gradle.plugin;

import com.varlanv.testnameconvention.commontest.DataTable;
import com.varlanv.testnameconvention.commontest.FunctionalTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNameConventionPluginFunctionalTest implements FunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    void abc(DataTable dataTable) {
        runGradleRunnerFixture(
            dataTable,
            List.of("test"),
            (fixture) -> {
                Files.writeString(fixture.rootBuildFile(), """
                    plugins {
                        id("java")
                        id("com.varlanv.testnameconvention-gradle-plugin")
                    }
                    
                    repositories {
                        mavenCentral()
                    }
                    
                    dependencies {
                        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
                        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
                    }
                    
                    test {
                        useJUnitPlatform()
                    }
                    """);
                var javaDir = Files.createDirectories(fixture.subjectProjectDir().resolve("src").resolve("test").resolve("java").resolve("somepackage"));
                var someTestClass = Files.createFile(javaDir.resolve("SomeTest.java"));
                Files.writeString(someTestClass, """
                    package somepackage;
                    import org.junit.jupiter.api.Test;
                    
                    public class SomeTest {
                    
                        @Test
                        void someMethodTest() {
                            System.out.println("someMethodTest");
                        }
                    }
                    
                    """);
                Files.writeString(fixture.settingsFile(), """
                    rootProject.name = "functional-test"
                    """);

                var result = build(fixture.runner());

                assertThat(result.getOutput()).contains("Hello World!");
            }
        );
    }
}
