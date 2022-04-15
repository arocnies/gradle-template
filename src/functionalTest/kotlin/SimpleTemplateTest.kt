import org.gradle.testkit.runner.TaskOutcome
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(Parameterized::class)
class SimpleTemplateTest(private val engineInstantiationExpression: EngineInstantiationExpression) {
    companion object {
        @Parameterized.Parameters(name = "{index}: Engine \"{0}\"")
        @JvmStatic
        fun getTemplateEngines(): List<EngineInstantiationExpression> = engineExpressions
    }

    @Test
    fun `templates file with project property`() {
        val gradleProject = GradleProject()
        gradleProject.importLines += engineInstantiationExpression.importLine
        gradleProject.expectedFiles += listOf(
            ExpectedFile(
                source = "src/templates/example.txt.ftl",
                dest = "build/templates/example.txt",
                initialContent = "Hello ${'$'}{properties.example}\nThis is a ${'$'}{test}",
                expectedContent = "Hello World!\nThis is a template"
            )
        )
        gradleProject.buildFileBody += """
            tasks.register("testTemplating", TemplateTask) {
                engine = ${engineInstantiationExpression.expression}
                data += [test: "template"]
                from('src/templates')
                into('build/templates')
                rename("(.+).ftl", "\${'$'}1")
            }
        """.trimIndent()
        gradleProject.properties += "example" to "World!"
        gradleProject.writeProject()
        gradleProject.run("testTemplating", "--stacktrace").also { result ->
            gradleProject.assertAllFilesHaveExpectedContent()
            assert(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
        }
    }
}