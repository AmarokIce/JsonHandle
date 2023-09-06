package club.someoneice.jsonprocessor.api

import club.someoneice.json.node.JsonNode
import club.someoneice.jsonprocessor.multiple.JsonProcessorCore
import com.google.common.collect.Maps

interface IJsonListCommand {
    fun doCommand(core: JsonProcessorCore, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap())
}