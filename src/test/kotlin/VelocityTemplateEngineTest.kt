import dev.anies.gradle.template.velocity.VelocityTemplateEngine
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class VelocityTemplateEngineTest {
    @Test
    fun `Velocity templates basic file`() {
        val engine = VelocityTemplateEngine()
        val source = File(javaClass.getResource("velocity.template").toURI()).toPath()
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