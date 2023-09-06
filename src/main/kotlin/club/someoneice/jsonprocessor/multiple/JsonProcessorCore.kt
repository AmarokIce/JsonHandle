package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.StringNode
import club.someoneice.jsonprocessor.api.IJsonCommand
import club.someoneice.jsonprocessor.api.IJsonEvent
import club.someoneice.jsonprocessor.api.IJsonListCommand
import club.someoneice.jsonprocessor.simple.SimpleNodeEvent
import club.someoneice.jsonprocessor.exception.NonMainMethodException
import club.someoneice.jsonprocessor.json
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap

@Suppress("unused")
class JsonProcessorCore internal constructor(internal val fileManager: FileManager) {
    internal val variablePool: HashMap<String, JsonNode<*>> = HashMap()
    internal val eventPool: Multimap<String, IJsonEvent> = ArrayListMultimap.create()
    internal val commandPool: Multimap<String, IJsonCommand> = ArrayListMultimap.create()
    internal val listCommandPool: Multimap<String, IJsonListCommand> = ArrayListMultimap.create()

    internal val utilHandle :   JsonProcessorUtil     = JsonProcessorUtil(this)
    internal val methodHandle:  JsonProcessorMethod   = JsonProcessorMethod(this)
    internal val mixinHandle:   JsonProcessorMixin    = JsonProcessorMixin(this)

    fun registryCommand(name: String, cmd: IJsonCommand) {
        this.commandPool.put(name, cmd)
    }

    fun registryCommand(name: String, event: IJsonListCommand) {
        this.listCommandPool.put(name, event)
    }

    fun registryEvent(name: String, event: IJsonEvent) {
        this.eventPool.put(name, event)
    }


    fun runMain() {
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
            when (val command = node[0].toString().lowercase()) {
                "@set",
                "@var"      -> variableIn(utilHandle.stringUtil(node[1]).toString(), node[2])
                "@run"      -> json.tryPullArrayOrEmpty(utilHandle.stringUtil(node[1]))
                "@print",
                "@println"  -> printNode(node[1])
                "@array"    -> utilHandle.arrayCommandHandle(node)
                "@if"       -> utilHandle.processorIf(node)
                "@post",
                "@notice"   -> if (eventPool.containsKey(node[1].toString())) methodHandle.eventHandler(eventPool[node[1].toString()])
                "@event"    -> registryEvent(node[1].toString(), SimpleNodeEvent(node[2].asTypeNode() as StringNode))
                "@mixin"    -> mixinHandle.mixin(node[1].asTypeNode() as StringNode, node[2].asTypeNode() as StringNode, node[3].asTypeNode() as StringNode)
                else        -> {
                    if (commandPool.containsKey(command)) {
                        commandPool[command].forEach {
                            it.doCommand(this)
                        }
                    }
                }
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