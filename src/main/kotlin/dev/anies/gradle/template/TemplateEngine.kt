package dev.anies.gradle.template

import org.gradle.api.file.FileTree
import java.nio.file.Path

/**
 * The engine responsible to perform the templating on the provided files.
 * A [TemplateEngine] is expected to perform templating on the most recent loaded files.
 * The type param [T] defines what object is used for configuring the engine (see [configure])
 */
interface TemplateEngine<out T> {
    /**
     * Prepares the [TemplateEngine] to template the provided files and only the provided files.
     * Previous calls to [load] should be cleared.
     */
    fun load(fileTree: FileTree)

    /**
     * Template the file from [source] and write the output to [destination].
     * @return True if the templating was successful.
     */
    fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean

    /**
     * Provides callers configuration extensions. This method should not be required.
     * If not called before other methods, the engine should use a default configuration.
     * Calls to [configure] should reset previous calls to configuration.
     */
    fun configure(block: T.() -> Unit = {})
}