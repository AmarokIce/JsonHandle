package club.someoneice.jsonprocessor.core.command

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler

class PrivateVar: IJsonHandler {
    override fun handler(fileMethods: MapNode, privateMethod: MapNode, commandNode: ArrayNode, variablePool: MapNode, privateVariablePool: MapNode): JsonNode<*>? {
        privateVariablePool.put(commandNode[1].toString(), commandNode[2])
        return null
    }
}