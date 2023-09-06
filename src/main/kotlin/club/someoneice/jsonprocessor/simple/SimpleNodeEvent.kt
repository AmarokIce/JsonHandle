package club.someoneice.jsonprocessor.simple

import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.StringNode
import club.someoneice.jsonprocessor.api.IJsonEvent
import club.someoneice.jsonprocessor.multiple.JsonProcessorCore

class SimpleNodeEvent(val node: StringNode): IJsonEvent {
    override fun doEvent(core: JsonProcessorCore, pool: HashMap<String, JsonNode<*>>) {
        core.utilHandle.stringUtil(node, pool)
    }
}