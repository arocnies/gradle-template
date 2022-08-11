package folder

import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.relativeTo
import kotlin.io.path.toPath

/**
 * Represents a prepared Gradle project with information.
 * @param sourceRoot path to the original directory the test project was created from.
 * @param projectDir path the test project and where it should be run.
 * @param expectedFiles path to the root dir of files which should match the files in [projectDir] after being run.
 */
data class TestProject(
    val sourceRoot: Path,
    val testRoot: Path,
    val expectedFiles: List<Path>,
    val runner: GradleRunner,
    val projectDir: Path
) {
    fun getUnmatchedFiles(): List<Path> {
        val unmatchedPaths: MutableList<Path> = mutableListOf()
        for (ef in expectedFiles) {
            val expectedFile = sourceRoot.resolve(ef).toFile()
            val actualFile = testRoot.resolve(ef).toFile()
            if (!actualFile.canRead() || !expectedFile.readText().contentEquals(actualFile.readText())) {
                unmatchedPaths.add(ef)
            }
        }
        return unmatchedPaths
    }
}

/**
 * Creates a new [TestProject] using resource paths.
 */
fun createTestProject(projectRoot: String, expectRoot: String): TestProject {
    val expectRootPath = getResourceFile(expectRoot) ?: error("Could not find resource at $expectRoot")
    val projectRootPath = getResourceFile(expectRoot) ?: error("Could not find resource at $projectRoot")

    val testTmpDir = copyResourcesToTempDir(projectRoot)
    val expectFiles = getFilesUnderResource(expectRoot)!! // We already check for this resource above.
        .map { it.relativeTo(expectRootPath) }
        .toList()

    val runner = GradleRunner.create()
    runner.withProjectDir(testTmpDir.toFile())
    runner.withDebug(true)
    runner.withPluginClasspath()

    return TestProject(projectRootPath, testTmpDir, expectFiles, runner, testTmpDir)
}

private fun getResourceFile(name: String) = TestProject::class.java.getResource(name)?.toURI()?.toPath()

private fun copyResourcesToTempDir(resourcePath: String): Path {
    val testProject = getResourceFile(resourcePath)?.toFile()
    val tempDir = createTempDirectory(prefix = testProject?.nameWithoutExtension)
    testProject?.copyRecursively(tempDir.toFile(), true)
    return tempDir
}

private fun getFilesUnderResource(name: String) = getResourceFile(name)
    ?.toFile()
    ?.walkTopDown()
    ?.filter { it.isFile }
    ?.map { it.toPath() }