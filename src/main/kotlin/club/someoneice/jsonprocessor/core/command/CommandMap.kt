package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.json.node.StringNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore

class CommandMap: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*>? {
        fun checkAndGeValue(value: JsonNode<*>, it: JsonNode<*>) {
            val type = value.asTypeNode().type
            val multiVariable = MapNode()
            privateVariablePool.obj.forEach(multiVariable::put)
            multiVariable.put("it", it)

            val multiMethod = MapNode()
            fileMethods.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            if (type == JsonNode.NodeType.String) {
                var name = value.toString()
                if (name.startsWith("#")) {
                    name = name.replaceFirst("#", "")
                    ProcessorCore.findMethodAndReturnValue(multiMethod, MapNode(), name, multiVariable, false)
                }
            }
            if (type == JsonNode.NodeType.Array) {
                ProcessorCore.loadMethod(multiMethod, "#IN-LINE", value as ArrayNode, false, privateVariablePool)!!
            }
        }

        val command = commandNode[1].toString()
        when(command.lowercase()) {
            "@create"   -> variablePool.put(commandNode[2].toString(), MapNode())
            "@put"      -> (variablePool.get(commandNode[2].toString()) as MapNode).put(commandNode[3].toString(), commandNode[4])
            "@get"      -> return (variablePool.get(commandNode[2].toString()) as MapNode).obj[commandNode[3].toString()]
            "@remove"   -> (variablePool.get(commandNode[2].toString()) as MapNode).obj.remove(commandNode[3].toString())
            "@for"      -> (variablePool.get(commandNode[2].toString()) as MapNode).obj.keys.forEach { checkAndGeValue(commandNode[3], StringNode(it)) }
        }

        return null
    }
}