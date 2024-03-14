package club.someoneice.jsonprocessor.core

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.jsonprocessor.api.IJsonHandler
import club.someoneice.jsonprocessor.core.command.*
import club.someoneice.jsonprocessor.exception.JsonProcessorException
import club.someoneice.jsonprocessor.util.Json
import kotlin.jvm.optionals.getOrNull

object ProcessorCore {
    private val commandMap: HashMap<String, IJsonHandler> = HashMap()
    private val variablePool = MapNode()

    init {
        init()
    }

    fun main() {
        val mainMethods = FileManager.filesNode.values.stream().filter { it.has("main") }.findFirst().getOrNull() ?: return
        val mainList = Json.tryPullArrayOrEmpty(mainMethods["main"]) ?: return
        loadMethod(mainMethods, "main", mainList)
    }

    fun findMain(methods: MapNode) {
        val mainMethods = Json.tryPullObjectOrEmpty(if (methods.has("main")) methods["main"] else return) ?: return
        val mainList = Json.tryPullArrayOrEmpty(mainMethods["main"]) ?: return
        loadMethod(mainMethods, "main", mainList)
    }

    fun loadMethod(methods: MapNode, methodName: String, methodRaw: ArrayNode, shouldReturn: Boolean = false, privateVariablePool: MapNode = MapNode()): JsonNode<*>? {
        var returnValue: JsonNode<*>? = null

        val privateMethods = MapNode()
        val method = if (methodName == "#IN-LINE") ArrayNode().apply { add(methodRaw) } else methodRaw
        method.obj.forEach {
            if (it.asTypeNode().type != JsonNode.NodeType.Array) return@forEach
            it as ArrayNode
            if (it[0].toString().startsWith("@method")) {
                val name = it[0].toString().split(":")[1]
                privateMethods.put(name, it[1] as MapNode)
                return@forEach
            }

            val commandName = it[0].toString().replace("@", "").lowercase()
            if (commandMap.containsKey(commandName))
                returnValue = commandMap[commandName]!!.handler(methods, privateMethods, it, variablePool, privateVariablePool)
            returnValue ?: return@forEach
            return returnValue
        }

        return if (!shouldReturn) null else throw JsonProcessorException(methodName, "Must have return value here!")
    }

    fun findMethodAndReturnValue(fileMethod: MapNode, privateMethod: MapNode, command: String, privateVariablePool: MapNode, shouldReturn: Boolean = true): JsonNode<*>? {
        if (command.contains("#")) {
            val commandList = command.split("#")
            val method = if (FileManager.filesNode.contains(commandList[0])) FileManager.filesNode[commandList[0]]!![commandList[1]]
                        else throw JsonProcessorException(command, "Cannot find file in method: ${commandList[0]}")
            return loadMethod(fileMethod, commandList[1], method as ArrayNode, true)!!
        }

        if (privateMethod.has(command)) {
            val multiMethod = MapNode()
            fileMethod.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            return loadMethod(multiMethod, command, privateMethod.get(command) as ArrayNode, shouldReturn, privateVariablePool)
        }

        if (fileMethod.has(command)) {
            val multiMethod = MapNode()
            fileMethod.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            return loadMethod(multiMethod, command, fileMethod.get(command) as ArrayNode, shouldReturn, privateVariablePool)
        }

        throw JsonProcessorException(command, "Cannot find file in method: $command")
    }

    private fun init() {
        commandMap["array"]         = CommandArray()
        commandMap["map"]           = CommandMap()
        commandMap["calculator"]    = Calculator()
        commandMap["greater"]       = Greater()
        commandMap["less"]          = Less()
        commandMap["is"]            = Is()
        commandMap["isnot"]         = IsNot()
        commandMap["if"]            = If()
        commandMap["return"]        = Return()
        commandMap["privateVar"]    = PrivateVar()
        commandMap["var"]           = Var()
        commandMap["println"]       = Println()
        commandMap["print"]         = Println()
        commandMap["run"]           = Run()
        commandMap["add"]           = Add()
        commandMap["sub"]           = Sub()
        commandMap["mul"]           = Mul()
        commandMap["div"]           = Div()
    }

    @Suppress("unused")
    fun preRegisterCommand(name: String, handler: IJsonHandler) {
        commandMap[name.lowercase()] = handler
    }

    fun preAddVariable(name: String, value: JsonNode<*>) {
        this.variablePool.put(name, value)
    }
}