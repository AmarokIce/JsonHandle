package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.BooleanNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore
import club.someoneice.jsonprocessor.exception.JsonProcessorException

class Is: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*> {
        fun checkAndGeValue(value: JsonNode<*>): JsonNode<*> {
            val type = value.asTypeNode().type
            try {
                if (type == JsonNode.NodeType.String) {
                    var name = value.toString()
                    if (name.startsWith("%")) {
                        name = name.replaceFirst("%", "")
                        return if (privateVariablePool.has(name)) privateVariablePool[name] else variablePool[name]
                    }
                    else if (name.startsWith("#")) {
                        name = name.replaceFirst("#", "")
                        return ProcessorCore.findMethodAndReturnValue(fileMethods, privateMethod, name, privateVariablePool)!!
                    }
                }

                if (type == JsonNode.NodeType.Array) {
                    val multiMethod = MapNode()
                    fileMethods.obj.forEach(multiMethod::put)
                    privateMethod.obj.forEach(multiMethod::put)
                    return ProcessorCore.loadMethod(multiMethod, "#IN-LINE", value as ArrayNode, true, privateVariablePool)!!
                }
            } catch (_: JsonProcessorException) {}
            return value
        }

        val value1 = checkAndGeValue(commandNode[1]).obj
        val value2 = checkAndGeValue(commandNode[2]).obj

        return BooleanNode(value1 == value2)
    }
}