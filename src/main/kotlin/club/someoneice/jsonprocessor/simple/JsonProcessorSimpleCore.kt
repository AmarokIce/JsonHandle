package club.someoneice.jsonprocessor.simple

import club.someoneice.json.JSON
import club.someoneice.json.node.*
import club.someoneice.jsonprocessor.exception.NonVariableInPoolException
import club.someoneice.jsonprocessor.goFalse
import java.io.File

@Suppress("unused")
class JsonProcessorSimpleCore(private val json: JSON, private val node: MapNode, private val fileName: String = "Unknown") {
    constructor(json: JSON, file: File) : this(json, json.tryPullObjectOrEmpty(json.parse(file)), file.name)
    constructor(json: JSON, st: String) : this(json, json.tryPullObjectOrEmpty(st))

    private val variablePool: HashMap<String, JsonNode<*>> = HashMap()

    init {
        if (node.has("main")) {
            val main: ArrayNode = if (node["main"].type == JsonNode.NodeType.Array) json.tryPullArrayOrEmpty(node.get("main")) else ArrayNode()

            main.obj.forEach {
                mainJsonHandle(it)
            }
        }
    }

    private fun mainJsonHandle(value: JsonNode<*>) {
        when (value.type) {
            JsonNode.NodeType.Map -> {
                json.tryPullObjectOrEmpty(value).obj.forEach {
                    when (it.key.lowercase()) {
                        "@set",
                        "@var"      -> setVariable(it.value)

                        "@run"      -> it.value.processor()
                        "@println"  -> println(it.value)
                        "@array"    -> arraySetting(it.value)
                    }
                }
            }

            JsonNode.NodeType.Array -> {
                val list = json.tryPullArrayOrEmpty(value)
                when(list[0].toString().lowercase()) {
                    "@set",
                    "@var"      -> setVariable(list[1])

                    "@run"      -> list[1].processor()
                    "@println"  -> println(list[1])
                    "@array"    -> arraySetting(list[1])
                }
            }

            else -> return
        }
    }

    /* Work stream */

    /**
     * Println
     * The value will println after processor.
     */
    private fun println(value: JsonNode<*>) {
        println(value.processor().toString())
    }

    /**
     * SetVariable
     * The value is an ArrayNode.
     *
     * List:
     *  [0]: Name of variable.
     *  [1]: Node of value.
     * */
    private fun setVariable(value: JsonNode<*>) {
        json.tryPullArrayOrEmpty(value).apply {
            this.isEmpty.goFalse {
                variablePool[this[0].toString()] = this[1]?.processor() ?: throw throw NonVariableInPoolException(fileName, "Here no variable in array 2!")
            }
        }
    }

    /**
     * Array setting
     *
     * List:
     *  [0]: Command of set.
     *  [1]: Name for array.
     *  [2]: Value for do.
     */
    private fun arraySetting(value: JsonNode<*>) {
        json.tryPullArrayOrEmpty(value).apply {
            this.isEmpty.goFalse {
                val arrayName = this[1].toString()
                when (this[0].toString()) {
                    "@create"   -> variablePool[arrayName] = ArrayNode()
                    "@add"      -> findArrayOrThrow(arrayName).add(this[2].processor())
                    "@remove"   -> if (this[2].type == JsonNode.NodeType.Int) {
                        findArrayOrThrow(arrayName).obj.removeAt(this[2].obj as Int)
                    } else {
                        findArrayOrThrow(arrayName).obj.remove(this[2].processor())
                    }

                    "@for"      -> {
                        val target = json.tryPullArrayOrEmpty(this[2])
                        val list = findArrayOrThrow(this[1].toString())

                        target.obj.isEmpty().goFalse {
                            list.obj.forEach {variable ->
                                target.obj.forEach {
                                    processorMethod(json.tryPullArrayOrEmpty(it), variable)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /* Processor variable */

    private fun processorMethod(value: ArrayNode, target: JsonNode<*>? = null): JsonNode<*> {
        val insidePool: HashMap<String, JsonNode<*>> = HashMap()

        value.obj.forEach { jsonNode ->
            when (jsonNode.type) {
                JsonNode.NodeType.Map -> {
                    json.tryPullObjectOrEmpty(jsonNode).obj.forEach {
                        when (it.key.toString().lowercase()) {
                            "@println" -> println(it.value.processor(target, insidePool = insidePool).toString())
                            "@overvar" -> setVariable(it.value.processor(target, insidePool = insidePool))
                            "@run" -> it.value.processor(target, insidePool = insidePool)
                            "@var" -> {
                                json.tryPullArrayOrEmpty(value).apply {
                                    this.isEmpty.goFalse {
                                        insidePool[this[0].toString()] =
                                            this[1]?.processor(target) ?: throw throw NonVariableInPoolException(
                                                fileName,
                                                "Here no variable in array 2!"
                                            )
                                    }
                                }
                            }

                            "@return" -> return it.value.processor(target, insidePool = insidePool)
                        }
                    }
                }

                JsonNode.NodeType.Array -> {
                    val list = json.tryPullArrayOrEmpty(jsonNode).obj
                    when (list[0].toString().lowercase()) {
                        "@println" -> println(list[1].processor(target, insidePool = insidePool).toString())
                        "@overvar" -> setVariable(list[1].processor(target, insidePool = insidePool))
                        "@run" -> list[1].processor(target, insidePool = insidePool)
                        "@var" -> {
                            json.tryPullArrayOrEmpty(list[1]).apply {
                                this.isEmpty.goFalse {
                                    insidePool[this[0].toString()] =
                                        this[1]?.processor(target) ?: throw throw NonVariableInPoolException(
                                            fileName,
                                            "Here no variable in array 2!"
                                        )
                                }
                            }
                        }

                        "@return" -> return list[1].processor(target, insidePool = insidePool)
                    }
                }

                else                    -> return JsonNode.NULL
            }
        }

        return JsonNode.NULL
    }

    private fun processorArrayVariable(value: ArrayNode): JsonNode<*> {
        return when(value[0].toString().lowercase()) {
            "@array" -> {
                when (value[1].toString().lowercase()) {
                    "@size" -> IntegerNode(findArrayOrThrow(value[2].toString()).obj.size)
                    "@get" ->  findArrayOrThrow(value[2].toString()).get(value[3].toString().toInt())

                    else -> StringNode(value.toString())
                }
            }


            "@if"   -> processorIf(value[1].asTypeNode() as ArrayNode, value[2].asTypeNode() as ArrayNode, value[3].asTypeNode() as ArrayNode)
            "@add"  -> DoubleNode(value[1].processor().obj.toString().toDouble() + value[2].processor().obj.toString().toDouble())
            "@min"  -> DoubleNode(value[1].processor().obj.toString().toDouble() - value[2].processor().obj.toString().toDouble())
            "@mul"  -> DoubleNode(value[1].processor().obj.toString().toDouble() * value[2].processor().obj.toString().toDouble())
            "@exc"  -> DoubleNode(value[1].processor().obj.toString().toDouble() / value[2].processor().obj.toString().toDouble())

            else -> StringNode(value.toString())
        }
    }


    private fun processorObjectVariable(value: MapNode): JsonNode<*> {
        val insidePool: HashMap<String, JsonNode<*>> = HashMap()
        value.obj.forEach {
            when(it.key.toString().lowercase()) {
                "@println"  -> println(it.value.processor(insidePool = insidePool).toString())
                "@overvar"  -> setVariable(it.value.processor(insidePool = insidePool))
                "@run"      -> it.value.processor(insidePool = insidePool)
                "@var"      -> {
                    json.tryPullArrayOrEmpty(value).apply {
                        this.isEmpty.goFalse {
                            insidePool[this[0].toString()] = this[1]?.processor() ?: throw throw NonVariableInPoolException(fileName, "Here no variable in array 2!")
                        }
                    }
                }

                "@return"   -> it.value.processor(insidePool = insidePool)
            }
        }

        return JsonNode.NULL
    }

    private fun processorStringVariable(value: StringNode, obj: JsonNode<*>? = null, insidePool: HashMap<String, JsonNode<*>>): JsonNode<*> {
        var original = value.obj
        insidePool.keys.forEach {
            val name = "%$it"
            original = original.replace(name, insidePool[it]!!.toString())
        }

        if (obj != null) original.replace("%it", obj.processor().toString())

        return if (original[0] == '#') {
            val funName = original.replaceFirst("#", "")
            if (node.obj.containsKey(funName)) {
                return processorMethod(json.tryPullArrayOrEmpty(node.get(funName)))
            } else StringNode(original)
        } else StringNode(original)
    }

    /**
     * Processor the IF command. <br />
     * Its will check ArrayNode 2 as condition. If recall it "true", run ArrayNode 3, or ArrayNode 4.
     */
    private fun processorIf(flag: ArrayNode, whenTrue: ArrayNode, whenFalse: ArrayNode): JsonNode<*> {
        val flagVar = when(flag[0].toString().lowercase()) {
            "@is"       -> flag[1].processor() == flag[2].processor()
            "@isnot"    -> flag[1].processor() != flag[2].processor()
            "@greater"  -> flag[1].processor().obj.toString().toDouble() > flag[2].processor().obj.toString().toDouble()
            "@less"     -> flag[1].processor().obj.toString().toDouble() < flag[2].processor().obj.toString().toDouble()
            else        -> if (flag[0].type == JsonNode.NodeType.Boolean) flag[0].obj as Boolean else false
        }

        return if (flagVar) processorArrayVariable(whenTrue) else processorArrayVariable(whenFalse)
    }

    private fun JsonNode<*>.processor(obj: JsonNode<*>? = null, insidePool: HashMap<String, JsonNode<*>> = HashMap()): JsonNode<*> {
        insidePool.putAll(variablePool)
        return when(this.type) {
            JsonNode.NodeType.Array     -> processorArrayVariable(json.tryPullArrayOrEmpty(this))
            JsonNode.NodeType.Map       -> processorObjectVariable(json.tryPullObjectOrEmpty(this))
            JsonNode.NodeType.String    -> processorStringVariable(this.asTypeNode() as StringNode, obj, insidePool)

            else                        -> this.asTypeNode()
        }
    }

    private fun findArrayOrThrow(name: String): ArrayNode {
        return json.tryPullArrayOrEmpty(variablePool[name] ?: throw NonVariableInPoolException(fileName, "Cannot find variable ArrayType with name $name !"))
    }
}