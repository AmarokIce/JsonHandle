package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore

class CommandArray: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*>? {
        fun checkAndGeValue(value: JsonNode<*>, it: JsonNode<*>) {
            val type = value.asTypeNode().type
            val multiMethod = MapNode()
            fileMethods.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            multiMethod.put("it", it)
            if (type == JsonNode.NodeType.String) {
                var name = value.toString()
                if (name.startsWith("#")) {
                    name = name.replaceFirst("#", "")
                    ProcessorCore.findMethodAndReturnValue(multiMethod, multiMethod, name, privateVariablePool, false)
                }
            }
            if (type == JsonNode.NodeType.Array) {
                ProcessorCore.loadMethod(multiMethod, "#IN-LINE", value as ArrayNode, false, privateVariablePool)!!
            }
        }

        val command = commandNode[1].toString()
        when(command.lowercase()) {
            "@create"   -> variablePool.put(commandNode[2].toString(), ArrayNode())
            "@add"      -> (variablePool.get(commandNode[2].toString()) as ArrayNode).add(commandNode[3])
            "@get"      -> return (variablePool.get(commandNode[2].toString()) as ArrayNode).get(commandNode[3].toString().toInt())
            "@remove"   -> (variablePool.get(commandNode[2].toString()) as ArrayNode).obj.removeAt(commandNode[3].toString().toInt())
            "@for"      -> (variablePool.get(commandNode[2].toString()) as ArrayNode).obj.forEach { checkAndGeValue(commandNode[3], it) }
        }

        return null
    }
}