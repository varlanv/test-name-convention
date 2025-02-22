package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.commontest.*;
import com.varlanv.testkonvence.commontest.sample.Sample;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.SAME_THREAD)
@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
class TestKonvencePluginFunctionalTest implements FunctionalTest {

    String defaultBuildGradleConfig = groovy("""
        plugins {
            id("java")
            id("com.varlanv.test-konvence")
        }
        
        repositories {
            mavenLocal()
            mavenCentral()
        }
        
        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
            testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        }
        
        test {
            useJUnitPlatform()
        }
        """
    );

    String defaultSettingsGradleConfig = groovy("""
        rootProject.name = "functional-test"
        """);


    @TestFactory
    Stream<DynamicTest> fromSamples() {
        return TestSamples.testSamples().stream()
            .map(sample -> DynamicTest.dynamicTest(sample.description(), () -> {
                sample.consume(consumableSample -> {
                    runGradleRunnerFixture(
                        new DataTable(false, false, false, TestGradleVersions.current()),
                        List.of("test"),
                        (fixture) -> {
                            Files.writeString(
                                fixture.settingsFile(),
                                defaultSettingsGradleConfig
                            );

                            Files.writeString(
                                fixture.rootBuildFile(),
                                defaultBuildGradleConfig
                            );
                            var javaDir = Files.createDirectories(fixture.subjectProjectDir().resolve("src").resolve("test").resolve("java"));
                            for (var sampleSourceFile : consumableSample.sources()) {
                                var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                Files.createDirectories(sourceFile.getParent());
                                Files.move(sampleSourceFile.path(), sourceFile);
                            }
                            build(fixture.runner(), GradleRunner::build);
                            for (var sampleSourceFile : consumableSample.sources()) {
                                var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                                var sourceFile = javaDir.resolve(relativeSourceFilePath);
                                var modifiedSourceFileContent = Files.readString(sourceFile);
                                assertThat(modifiedSourceFileContent).isEqualTo(sampleSourceFile.expectedTransformation());
                            }
                        }
                    );
                });
            }));
    }

    @Test
    @DisplayName("If requesting dry enforce with failing and there is enforcement fail, then should fail build")
    void should_fail_when_dry_enforce_with_failing_and_there_is_enforcement_fail() {
        Sample sample = TestSamples.testSamples().stream()
            .filter(s -> Objects.equals(s.description(), "Should replace method name if found"))
            .findFirst()
            .orElseThrow();
        sample.consume(consumableSample -> {
            runGradleRunnerFixture(
                new DataTable(false, false, false, TestGradleVersions.current()),
                List.of("testKonvenceDryEnforceWithFailing"),
                (fixture) -> {
                    Files.writeString(
                        fixture.settingsFile(),
                        defaultSettingsGradleConfig
                    );

                    Files.writeString(
                        fixture.rootBuildFile(),
                        defaultBuildGradleConfig
                    );
                    var javaDir = Files.createDirectories(fixture.subjectProjectDir().resolve("src").resolve("test").resolve("java"));
                    var sampleSourceFile = consumableSample.sourceFile();
                    var contentBefore = sampleSourceFile.content();
                    var relativeSourceFilePath = consumableSample.dir().relativize(sampleSourceFile.path());
                    var sourceFile = javaDir.resolve(relativeSourceFilePath);
                    Files.createDirectories(sourceFile.getParent());
                    Files.move(sampleSourceFile.path(), sourceFile);

                    var buildResult = build(fixture.runner(), GradleRunner::buildAndFail);
                    assertThat(buildResult.getOutput()).contains("found test name mismatch in");

                    var newSourceFileContent = Files.readString(sourceFile);
                    assertThat(newSourceFileContent).isEqualTo(contentBefore);
                }
            );
        });
    }
}
