package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.Pair
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.json
import com.google.common.collect.Lists
import java.io.File

@Suppress("unused")
class ProcessorEntry(mainFile: File, dir: File = File(mainFile.path.replaceAfter(mainFile.name, ""))) {
    private val jsonCore: JsonProcessorCore by lazy {
        val pairMain: Pair<String, MapNode> = Pair(mainFile.nameWithoutExtension, json.tryPullObjectOrEmpty(json.parse(mainFile)))
        val fileManager = FileManager(pairMain)

        if (dir.isDirectory) {
            getFiles(dir).forEach {
                fileManager.addFile(it)
            }
        }

        JsonProcessorCore(fileManager)
    }

    private fun getFiles(dir: File): List<File> {
        val list: ArrayList<File> = Lists.newArrayList()
        dir.listFiles()!!.forEach {
            if (it.isDirectory) list.addAll(getFiles(it))
            else if (it.extension == "json" || it.extension == "json5") list.add(it)
        }

        return list
    }

    fun get(): JsonProcessorCore = this.jsonCore
    fun getter(): JsonProcessorCore = this.jsonCore
}