package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.DoubleNode
import club.someoneice.json.node.IntegerNode
import club.someoneice.json.node.JsonNode
import club.someoneice.jsonprocessor.json
import com.google.common.collect.Maps

class JsonProcessorMethod internal constructor(private val core: JsonProcessorCore) {
    internal fun runMethod(node: ArrayNode): JsonNode<*> {
        val variableTemporary: HashMap<String, JsonNode<*>> = Maps.newHashMap()

        fun printNode(value: JsonNode<*>) {
            println(core.utilHandle.stringUtil(value, variableTemporary))
        }

        fun variableIn(name: String, jsonNode: JsonNode<*>) {
            if (jsonNode.asTypeNode().type == JsonNode.NodeType.Array) variableTemporary[name] = arrayUtil(jsonNode as ArrayNode, variableTemporary)
            else variableTemporary[name] = jsonNode
        }

        node.obj.forEach {
            val list = json.tryPullArrayOrEmpty(it)
            if (!list.isEmpty) {
                val command = list[0]
                when(command.toString().lowercase()) {
                    "@set",
                    "@var"          -> variableIn(list[1].toString(), list[2])
                    "@overvar"      -> core.variableIn(list[1].toString(), list[2])
                    "@run"          -> json.tryPullArrayOrEmpty(core.utilHandle.stringUtil(list[1]))
                    "@print",
                    "@println"      -> printNode(list[1])
                    "@array"        -> core.utilHandle.arrayCommandHandle(list, variableTemporary, true)
                    "@overarray"    -> core.utilHandle.arrayCommandHandle(list, variableTemporary)
                    "@if"           -> core.utilHandle.processorIf(node, variableTemporary)

                    "@mixin"        -> {/* TODO */}
                    "@return"       -> return core.utilHandle.stringUtil(list[1], variableTemporary)
                }
            }
        }

        return JsonNode.NULL
    }

    internal fun arrayUtil(node: ArrayNode, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap()): JsonNode<*> {
        val command = node[0]
        return when (command.toString().lowercase()) {
            "@add"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() + core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@min"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() - core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@mul"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() * core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@exc"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() / core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@calculator"   -> IntegerNode(Calculator.calculator(node[1].toString()))

            "@if"           -> core.utilHandle.processorIf(node, pool)
            "@array"        -> core.utilHandle.arrayCommandHandle(node, pool)

            else            -> JsonNode.NULL
        }
    }
}