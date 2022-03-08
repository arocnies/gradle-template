package dev.anies.gradle.template

import freemarker.cache.TemplateLoader
import org.gradle.api.file.FileTree
import java.io.File
import java.io.IOException
import java.io.Reader
import java.nio.charset.Charset

class FileTreeTemplateLoader(private val fileTree: FileTree) : TemplateLoader {
    private val readers = mutableMapOf<String, Reader>()

    override fun findTemplateSource(name: String?): Any? {
        if (name.isNullOrBlank()) return null
        return fileTree.associateBy { getTemplateKey(it) }[name]
    }

    override fun getLastModified(templateSource: Any?): Long {
        if (templateSource !is File) return -1
        return templateSource.lastModified()
    }

    override fun getReader(templateSource: Any, encoding: String?): Reader {
        if (templateSource !is File) throw IOException("Unable to get reader for $templateSource file")
        return getReader(templateSource, encoding)
    }

    override fun closeTemplateSource(templateSource: Any?) {
        if (templateSource !is File) throw IllegalArgumentException("Bad templateSource type ${templateSource?.javaClass} when expecting File")
        readers[getTemplateKey(templateSource)]?.close()
    }

    private fun getTemplateKey(file: File): String {
//        return file.toPath().absolute().relativize(Path.of(fileTree.asPath)).toString()
        return file.name
    }

    private fun getReader(file: File, encoding: String?): Reader {
        if (readers.containsKey(getTemplateKey(file))) readers[getTemplateKey(file)]?.close()
        val charset = if (encoding != null) charset(encoding) else Charset.defaultCharset()
        return file.reader(charset)
    }
}