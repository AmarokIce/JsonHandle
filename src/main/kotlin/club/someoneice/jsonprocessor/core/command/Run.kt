package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore

class Run: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*>? {
        val value = commandNode[1]
        val type = value.asTypeNode().type
        if (type == JsonNode.NodeType.String) {
            var name = value.toString()
            if (name.startsWith("#")) {
                name = name.replaceFirst("#", "")
                ProcessorCore.findMethodAndReturnValue(fileMethods, privateMethod, name, privateVariablePool, false)
            }
        }
        if (type == JsonNode.NodeType.Array) {
            val multiMethod = MapNode()
            fileMethods.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            ProcessorCore.loadMethod(multiMethod, "#IN-LINE", value as ArrayNode, false, privateVariablePool)
        }

        return null
    }
}