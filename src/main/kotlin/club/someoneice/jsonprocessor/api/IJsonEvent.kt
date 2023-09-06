package club.someoneice.jsonprocessor.api

import club.someoneice.json.node.JsonNode
import club.someoneice.jsonprocessor.multiple.JsonProcessorCore
import com.google.common.collect.Maps

interface IJsonEvent {
    fun doEvent(core: JsonProcessorCore, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap())
}