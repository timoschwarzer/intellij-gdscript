import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.jsoup.Jsoup.connect
import org.jsoup.nodes.Document
import java.io.File

open class GenerateDocumentationTask : DefaultTask() {

    @TaskAction
    fun run() {
        val directory = fetch("https://github.com/godotengine/godot/tree/master/doc/classes")
        val languageLink = "https://github.com/godotengine/godot/raw/master/modules/gdscript/doc_classes/@GDScript.xml"
        val links = collectLinks(directory) + languageLink
        val files = links.map { fetch(it) }
        val singletonNames = files
            .filter { isLanguageClass(it) }
            .flatMap { parseFields(it) }
            .map { it.name }
        val language = Language(
            constants = files
                .filter { isLanguageClass(it) }
                .flatMap { collectConstants(it) }
                .sortedBy { it.name },
            classes = files
                .filter { !isLanguageClass(it) }
                .filter { it.select("class").attr("name") !in singletonNames }
                .filter { isNotEmptyClass(it) }
                .map { parseClass(it) }
                .sortedBy { it.name },
            functions = files
                .filter { isLanguageClass(it) }
                .flatMap { parseMethods(it) }
                .sortedBy { it.name },
            singletons = files
                .filter { it.select("class").attr("name") in singletonNames }
                .map { parseClass(it) },
            primitiveClasses = files
                .filter { isPrimitiveClass(it) }
                .map { parseClass(it) }
                .sortedBy { it.name }
        )
        serializeToFile(language)
    }

    private fun fetch(url: String): Document {
        println(url)
        return connect(url).get()
    }

    private fun serializeToFile(language: Language) {
        val writer = File(JSON_LOCATION).writer()
        GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(language, writer)
        writer.flush()
    }

    companion object {
        const val JSON_LOCATION = "src/main/resources/completion.json"
    }

}

fun Project.registerGenerateCompletionDataTask() =
    tasks.register<GenerateDocumentationTask>("generateCompletion")
