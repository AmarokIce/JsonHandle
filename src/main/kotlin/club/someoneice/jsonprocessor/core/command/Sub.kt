package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.DoubleNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore
import club.someoneice.jsonprocessor.exception.JsonProcessorException

class Sub: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*> {
        fun checkAndGeValue(value: JsonNode<*>): JsonNode<*> {
            val type = value.asTypeNode().type

            if (type == JsonNode.NodeType.Double || type == JsonNode.NodeType.Float || type == JsonNode.NodeType.Int) return value
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

            throw JsonProcessorException(value.toString(), "Cannot processor Method!")
        }

        val value1 = checkAndGeValue(commandNode[1]).obj.toString().toDouble()
        val value2 = checkAndGeValue(commandNode[2]).obj.toString().toDouble()

        return DoubleNode(value1 - value2)
    }
}