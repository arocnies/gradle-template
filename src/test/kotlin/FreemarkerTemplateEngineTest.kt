import dev.anies.gradle.template.freemarker.FreemarkerTemplateEngine
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class FreemarkerTemplateEngineTest {
    @Test
    fun `Freemarker templates basic file`() {
        val engine = FreemarkerTemplateEngine()
        val source = File(javaClass.getResource("freemarker.template").toURI()).toPath()
        val dest = kotlin.io.path.createTempFile().toAbsolutePath()
        @Suppress("unused") val data = mapOf(
            "message" to "test-message",
            "obj" to object {
                val text = "obj-text"
                fun method() = "obj-method"
            }
        )
        val project = ProjectBuilder.builder().build()
        engine.load(project.fileTree(source))
        engine.processTemplate(source, dest, data)

        assertEquals(
            """
            Test message: test-message
            Test property objects: obj-text
            Test obj methods: obj-method
        """.trimIndent(),
            dest.readText()
        )
    }
}