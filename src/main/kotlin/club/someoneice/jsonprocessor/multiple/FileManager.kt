package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.Pair
import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.json
import java.io.File

@Suppress("unused")
class FileManager(internal val mainMap: Pair<String, MapNode>) {
    internal val fileMap: HashMap<String, MapNode> = HashMap()

    fun addFile(file: File) {
        if (file.isFile) {
            this.fileMap[file.nameWithoutExtension] = json.tryPullObjectOrEmpty(json.parse(file))
        }
    }

    fun hasFile(name: String): Boolean = this.fileMap.containsKey(name)

    fun getMainMethod(): ArrayNode = json.tryPullArrayOrEmpty(this.mainMap.value["main"])
    fun getMethod(name: String, methodName: String): ArrayNode = json.tryPullArrayOrEmpty(this.fileMap[name]?.get(methodName))
    fun getFileNode(name: String): MapNode = this.fileMap[name] ?: MapNode()
    fun getMainNode(): MapNode = mainMap.value
}