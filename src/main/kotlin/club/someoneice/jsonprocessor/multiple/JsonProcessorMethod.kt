package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.*
import club.someoneice.jsonprocessor.api.IJsonEvent
import club.someoneice.jsonprocessor.json
import club.someoneice.jsonprocessor.simple.SimpleNodeEvent
import com.google.common.collect.Maps

class JsonProcessorMethod internal constructor(private val core: JsonProcessorCore) {
    internal fun runMethod(node: ArrayNode, readyPool: HashMap<String, JsonNode<*>> = Maps.newHashMap()): JsonNode<*> {
        val variableTemporary: HashMap<String, JsonNode<*>> = Maps.newHashMap(readyPool)

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
                when(val command = list[0].toString().lowercase()) {
                    "@set",
                    "@var"          -> variableIn(list[1].toString(), list[2])
                    "@overvar"      -> core.variableIn(list[1].toString(), list[2])
                    "@run"          -> json.tryPullArrayOrEmpty(core.utilHandle.stringUtil(list[1]))
                    "@print",
                    "@println"      -> printNode(list[1])
                    "@array"        -> core.utilHandle.arrayCommandHandle(list, variableTemporary, true)
                    "@overarray"    -> core.utilHandle.arrayCommandHandle(list, variableTemporary)
                    "@if"           -> core.utilHandle.processorIf(node, variableTemporary)
                    "@mixin"        -> core.mixinHandle.mixin(node[1].asTypeNode() as StringNode, node[2].asTypeNode() as StringNode, node[3].asTypeNode() as StringNode)
                    "@post",
                    "@notice"       -> if (core.eventPool.containsKey(list[1].toString())) eventHandler(core.eventPool[list[1].toString()], variableTemporary)
                    "@event"        -> core.registryEvent(node[1].toString(), SimpleNodeEvent(node[2].asTypeNode() as StringNode))
                    "@return"       -> return core.utilHandle.stringUtil(list[1], variableTemporary)
                    else            -> {
                        if (core.commandPool.containsKey(command)) {
                            core.commandPool[command].forEach { cd ->
                                cd.doCommand(core, variableTemporary)
                            }
                        }
                    }
                }
            }
        }

        return JsonNode.NULL
    }

    internal fun arrayUtil(node: ArrayNode, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap()): JsonNode<*> {
        return when (val command = node[0].toString().lowercase()) {
            "@add"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() + core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@min"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() - core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@mul"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() * core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@exc"          -> DoubleNode(core.utilHandle.stringUtil(node[1], pool).toString().toDouble() / core.utilHandle.stringUtil(node[2], pool).toString().toDouble())
            "@calculator"   -> IntegerNode(Calculator.calculator(node[1].toString()))

            "@if"           -> core.utilHandle.processorIf(node, pool)
            "@array"        -> core.utilHandle.arrayCommandHandle(node, pool)
            "@mixin"        -> core.mixinHandle.mixin(node[1].asTypeNode() as StringNode, node[2].asTypeNode() as StringNode, node[3].asTypeNode() as StringNode)
            "@post",
            "@notice"       -> {
                if (core.eventPool.containsKey(node[1].toString())) {
                    eventHandler(core.eventPool[node[1].toString()], pool)
                    BooleanNode(true)
                }
                BooleanNode(false)
            }
            "@event"        -> {
                core.registryEvent(node[1].toString(), SimpleNodeEvent(node[2].asTypeNode() as StringNode))
                BooleanNode(true)
            }
            "@return"       -> core.utilHandle.stringUtil(node[1], pool)

            else            -> {
                if (core.listCommandPool.containsKey(command)) {
                    core.listCommandPool[command].forEach {
                        it.doCommand(core, pool)
                    }

                    BooleanNode(true)
                }

                BooleanNode(false)
            }
        }
    }

    internal fun eventHandler(eventList: MutableCollection<IJsonEvent>, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap()) {
        eventList.forEach {
            it.doEvent(core, pool)
        }
    }
}