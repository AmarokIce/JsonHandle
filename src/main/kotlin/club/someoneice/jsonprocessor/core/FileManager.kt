package club.someoneice.jsonprocessor.core

import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.exception.JsonProcessorException
import club.someoneice.jsonprocessor.util.Json
import java.io.File

object FileManager {
    val files: HashMap<String, File> = HashMap()
    val filesNode: HashMap<String, MapNode> = HashMap()

    private fun addFile(file: File) {
        files[file.nameWithoutExtension] = file
        filesNode[file.nameWithoutExtension] = Json.tryPullObjectOrEmpty(Json.parse(file))
    }

    fun scanFile(file: File) {
        if (file.isFile) {
            addFile(file)
            return
        }

        if (file.isDirectory) {
            file.listFiles()?.forEach(::scanFile) ?: return
            return
        }

        throw JsonProcessorException(file.nameWithoutExtension, "Unknown file or directory!")
    }
}