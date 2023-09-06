package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.StringNode
import club.someoneice.jsonprocessor.exception.NonMainMethodException
import club.someoneice.jsonprocessor.json

@Suppress("unused")
class JsonProcessorCore internal constructor(internal val fileManager: FileManager) {
    internal val variablePool: HashMap<String, JsonNode<*>> = HashMap()

    internal val utilHandle :   JsonProcessorUtil     = JsonProcessorUtil(this)
    internal val methodHandle:  JsonProcessorMethod   = JsonProcessorMethod(this)
    internal val mixinHandle:   JsonProcessorMixin    = JsonProcessorMixin(this)

    init {
        val main = fileManager.getMainMethod()
        if (main.isEmpty) throw NonMainMethodException(fileManager.mainMap.key, "Cannot find the main method in file ${fileManager.mainMap.key}")
        else core(main)
    }

    fun processorStringByUtil(str: String): JsonNode<*> = utilHandle.stringUtil(StringNode(str))
    fun processorStringByUtil(str: JsonNode<*>): JsonNode<*> = utilHandle.stringUtil(str)

    private fun core(methodArray: ArrayNode) {
        for (value in methodArray.obj) {
            val node = json.tryPullArrayOrEmpty(value)
            if (node.isEmpty) continue
            when (node[0].toString().lowercase()) {
                "@set",
                "@var"      -> variableIn(utilHandle.stringUtil(node[1]).toString(), node[2])
                "@run"      -> json.tryPullArrayOrEmpty(utilHandle.stringUtil(node[1]))
                "@print",
                "@println"  -> printNode(node[1])
                "@array"    -> utilHandle.arrayCommandHandle(node)
                "@pair"     -> {/* TODO */}
                "@if"       -> utilHandle.processorIf(node)

                // Top
                // Mixin用于交换方法内部实现。
                "@mixin"    -> {/* TODO */}

            }
        }
    }

    private fun printNode(value: JsonNode<*>) {
        println(this.utilHandle.stringUtil(value))
    }

    internal fun variableIn(name: String, jsonNode: JsonNode<*>, array: Boolean = false) {
        if (array) {
            this.variablePool[name] = jsonNode
            return
        }

        if (jsonNode.asTypeNode().type == JsonNode.NodeType.Array) this.variablePool[name] = methodHandle.arrayUtil(jsonNode as ArrayNode)
        else this.variablePool[name] = utilHandle.stringUtil(jsonNode)
    }
}