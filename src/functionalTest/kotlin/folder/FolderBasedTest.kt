package folder

import org.gradle.testkit.runner.TaskOutcome
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class FolderBasedTest(private val folderTest: TestProjectPaths) {
    companion object {
        @Parameterized.Parameters(name = "{index}: {0}")
        @JvmStatic
        fun getTemplateEngines(): List<TestProjectPaths> =
            listOf(
                TestProjectPaths("freemarker/project1", "expect"),
                TestProjectPaths("freemarker/project2", "expect"),
                TestProjectPaths("freemarker/project3", "expect"),
                TestProjectPaths("velocity/project1", "expect"),
                TestProjectPaths("velocity/project2", "expect"),
                TestProjectPaths("velocity/project3", "expect"),
            )
    }

    @Test
    fun `Test Template`() {
        val testProject = createTestProject(folderTest.projectRoot, folderTest.expectRoot)
        val result = testProject.runner.withArguments("testTemplating").build()
        assert(testProject.getUnmatchedFiles().isEmpty()) {
            "Failed to find match for files: ${testProject.getUnmatchedFiles()}\n\tat ${testProject.testRoot}"
        }
        assertTrue(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })

        val secondRun = testProject.runner.withArguments("testTemplating").build()
        assert(testProject.getUnmatchedFiles().isEmpty())
        assertTrue(secondRun.tasks.all { it.outcome == TaskOutcome.UP_TO_DATE })
    }
}

data class TestProjectPaths(val projectRoot: String, val expectRoot: String) {
    override fun toString(): String = "proj=$projectRoot, expect=$expectRoot"
}