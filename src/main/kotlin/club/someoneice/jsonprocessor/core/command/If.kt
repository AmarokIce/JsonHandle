package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.ProcessorCore
import club.someoneice.jsonprocessor.exception.JsonProcessorException

class If: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*>? {
        fun checkAndGeValue(value: JsonNode<*>, runBool: Boolean): JsonNode<*>? {
            val type = value.asTypeNode().type
            if (type == JsonNode.NodeType.String) {
                var name = value.toString()
                if (name.startsWith("%")) {
                    name = name.replaceFirst("%", "")
                    return if (privateVariablePool.has(name)) privateVariablePool[name] else variablePool[name]
                }
                else if (name.startsWith("#")) {
                    name = name.replaceFirst("#", "")
                    return ProcessorCore.findMethodAndReturnValue(fileMethods, privateMethod, name, privateVariablePool, runBool)
                }
            }
            if (type == JsonNode.NodeType.Array) {
                val multiMethod = MapNode()
                fileMethods.obj.forEach(multiMethod::put)
                privateMethod.obj.forEach(multiMethod::put)
                return ProcessorCore.loadMethod(multiMethod, "#IN-LINE", value as ArrayNode, runBool, privateVariablePool)
            }

            if (!runBool) return null else throw JsonProcessorException(value.toString(), "Cannot processor Method!")
        }

        if (checkAndGeValue(commandNode[1], true)!!.obj as Boolean) checkAndGeValue(commandNode[2], false)
        else if (commandNode.obj.size > 2) checkAndGeValue(commandNode[3], false)

        return null
    }
}