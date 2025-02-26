/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.privacysandbox.tools.apigenerator

import androidx.room.compiler.processing.util.Source
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PrivacySandboxApiGeneratorTest {
    @Test
    fun testSandboxSdk_compilesAndGeneratesExpectedOutput() {
        val descriptors =
            compileIntoInterfaceDescriptorsJar(
                *loadSourcesFromDirectory(inputTestDataDir).toTypedArray()
            )
        val aidlPath = System.getProperty("aidl_compiler_path")?.let(::Path)
            ?: throw IllegalArgumentException("aidl_compiler_path flag not set.")

        val generator = PrivacySandboxApiGenerator()

        val outputDir = Files.createTempDirectory("output").also { it.toFile().deleteOnExit() }
        generator.generate(descriptors, aidlPath, outputDir)
        val outputSources = loadSourcesFromDirectory(outputDir.toFile())

        assertCompiles(outputSources)

        val expectedSources = loadSourcesFromDirectory(outputTestDataDir)
        assertThat(outputSources.map(Source::relativePath))
            .containsExactlyElementsIn(
                expectedSources.map(Source::relativePath) + listOf(
                    "com/mysdk/ITestSandboxSdk.java",
                    "com/mysdk/IUnitTransactionCallback.java",
                    "com/mysdk/ICancellationSignal.java",
                    "com/mysdk/IIntTransactionCallback.java",
                    "com/mysdk/IFloatTransactionCallback.java",
                    "com/mysdk/ICharTransactionCallback.java",
                    "com/mysdk/IDoubleTransactionCallback.java",
                    "com/mysdk/IBooleanTransactionCallback.java",
                    "com/mysdk/IStringTransactionCallback.java",
                    "com/mysdk/ILongTransactionCallback.java",
                )
            )

        val outputSourceMap = outputSources.associateBy(Source::relativePath)
        for (expected in expectedSources) {
            assertThat(outputSourceMap[expected.relativePath]?.contents)
                .isEqualTo(expected.contents)
        }
    }

    private fun loadSourcesFromDirectory(directory: File): List<Source> {
        return directory.walk().filter { it.isFile }.map {
            val relativePath = directory.toPath().relativize(it.toPath()).toString()
            val qualifiedName = relativePath.removeSuffix(".${it.extension}").replace('/', '.')
            Source.load(file = it, qName = qualifiedName, relativePath = relativePath)
        }.toList()
    }

    companion object {
        val inputTestDataDir = File("src/test/test-data/input")
        val outputTestDataDir = File("src/test/test-data/output")
    }
}