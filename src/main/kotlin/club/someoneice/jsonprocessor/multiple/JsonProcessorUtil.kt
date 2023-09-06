package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.*
import club.someoneice.jsonprocessor.json
import com.google.common.collect.Maps

class JsonProcessorUtil internal constructor(private val core: JsonProcessorCore) {
    internal fun stringUtil(str: JsonNode<*>, variableTemporary: HashMap<String, JsonNode<*>> = Maps.newHashMap(), obj: JsonNode<*> = JsonNode.NULL): JsonNode<*> {
        val map = Maps.newHashMap<String, JsonNode<*>>(core.variablePool)
        if (variableTemporary.isNotEmpty()) map.putAll(variableTemporary)

        return when (str.asTypeNode().type) {
            JsonNode.NodeType.String -> {
                var original = str.toString()

                map.keys.forEach {
                    val name = "%$it"
                    original = original.replace(name, map[it]!!.toString())
                }

                if (obj != JsonNode.NULL) {
                    original.replace("%it", obj.toString())
                }

                val node =
                            if (original[0] == '%') {
                                map[original.replaceFirst("%", "")] ?: StringNode(original)
                            } else if (original[0] == '#') {
                                core.methodHandle.runMethod(json.tryPullArrayOrEmpty(core.fileManager.getMainNode().get(original.replaceFirst("#", ""))))
                            } else if (original[0] == '!') {
                                val fileName = original.substring(1, original.indexOf("#"))
                                val methodName = original.substring(original.indexOf("#") + 1)

                                core.methodHandle.runMethod(core.fileManager.getMethod(fileName, methodName))
                            } else StringNode(original)


                tryGetNode(node.toString())
            }

            JsonNode.NodeType.Array -> core.methodHandle.arrayUtil(json.tryPullArrayOrEmpty(str))
            else -> str
        }
    }

    internal fun arrayCommandHandle(node: ArrayNode, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap(), isPart: Boolean = false): JsonNode<*> {
        val commandLine = if (node[1].type == JsonNode.NodeType.Array) json.tryPullArrayOrEmpty(node[1]) else {
            val nod = ArrayNode()
            for (i in 1 until node.obj.size) {
                nod.add(node[i])
            }

            nod
        }

        val name = commandLine[1].toString()
        val command = commandLine[0].toString().lowercase()
        if (command == "@create") {
            if (isPart) {
                pool[name] = ArrayNode()
            } else core.variableIn(name, ArrayNode(), true)

            return BooleanNode(true)
        }

        val list = json.tryPullArrayOrEmpty(pool[name] ?: core.variablePool[name])

        return when (command) {
            "@add"      -> {
                list.add(commandLine[2].asTypeNode())
                BooleanNode(true)
            }
            "@remove"   -> {
                list.obj.remove(commandLine[2].asTypeNode())
                BooleanNode(true)
            }
            "@check"    -> list
            "@get"      -> list[commandLine[2].asTypeNode().toString().toInt()]
            "@size"     -> IntegerNode(list.obj.size)
            "@for"      -> {
                list.obj.forEach {
                    stringUtil(commandLine[2], pool, it)
                }

                BooleanNode(true)
            }

            else        -> BooleanNode(false)
        }
    }

    internal fun processorIf(node: ArrayNode, pool: HashMap<String, JsonNode<*>> = Maps.newHashMap()): JsonNode<*> {
        fun processorBoolean(pNode: ArrayNode): BooleanNode {
            fun handle(p1: JsonNode<*>, p2: JsonNode<*>): Boolean =
                if (p2 == JsonNode.NULL) core.utilHandle.stringUtil(p1, pool).toString().toBoolean()
                else core.utilHandle.stringUtil(p1, pool).asTypeNode().obj == core.utilHandle.stringUtil(p2, pool).asTypeNode().obj

            val command = pNode[0]
            return when (command.toString().lowercase()) {
                "@is"       -> BooleanNode(handle(pNode[1],  if (pNode.obj.size > 2) pNode[2] else JsonNode.NULL))
                "@isnot"    -> BooleanNode(!handle(pNode[1], if (pNode.obj.size > 2) pNode[2] else JsonNode.NULL))
                "@greater"  -> BooleanNode(core.utilHandle.stringUtil(pNode[1], pool).toString().toDouble() > core.utilHandle.stringUtil(pNode[2], pool).toString().toDouble())
                "@less"     -> BooleanNode(core.utilHandle.stringUtil(pNode[1], pool).toString().toDouble() < core.utilHandle.stringUtil(pNode[2], pool).toString().toDouble())
                else        -> BooleanNode(handle(pNode[1], JsonNode.NULL))
            }
        }

        for (step in json.tryPullArrayOrEmpty(node[2]).obj) {
            if (!processorBoolean(json.tryPullArrayOrEmpty(step)).obj) {
                return core.utilHandle.stringUtil(node[3], pool)
            }
        }

        return core.utilHandle.stringUtil(node[2], pool)
    }

    /* Private Zone*/

    private fun tryGetNode(str: String): JsonNode<*> {
        return if (!str.equals("true", ignoreCase = true) && str != "false") {
            val numberNode = getNumber(str)
            if (numberNode == JsonNode.NULL) StringNode(str) else numberNode
        } else {
            BooleanNode(str.toBoolean())
        }
    }

    private fun getNumber(str: String): JsonNode<*> {
        var has = false
        var e = false
        for (i in str.indices) {
            val c = str[i]
            if (c < '0' || c > '9') {
                if (c == '.') {
                    if (has) return JsonNode.NULL
                    has = true
                } else {
                    if (c != 'e' && c != 'E') {
                        if (c != 'd' && c != 'D') {
                            if (c != 'f' && c != 'F') {
                                return JsonNode.NULL
                            }
                            return if (i == str.length - 1) {
                                FloatNode(str.toFloat())
                            } else JsonNode.NULL
                        }
                        return if (i == str.length - 1) {
                            DoubleNode(str.toDouble())
                        } else JsonNode.NULL
                    }
                    if (e) return JsonNode.NULL

                    e = true
                }
            }
        }
        return if (has)
            DoubleNode(str.toDouble())
        else IntegerNode(str.toInt())
    }
}