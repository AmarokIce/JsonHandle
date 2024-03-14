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

    @OptIn(ExperimentalStdlibApi::class)
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

    fun loadMethod(methods: MapNode, methodName: String, method: ArrayNode, shouldReturn: Boolean = false, privateVariablePool: MapNode = MapNode()): JsonNode<*>? {
        var returnValue: JsonNode<*>? = null

        val privateMethods = MapNode()
        method.obj.forEach {
            if (it.asTypeNode().type != JsonNode.NodeType.Array) return@forEach
            it as ArrayNode
            if (it[1].asTypeNode().type == JsonNode.NodeType.Map) {
                privateMethods.put(it.get(0).toString(), it[1] as MapNode)
                return@forEach
            }

            val commandName = it[0].toString().replace("@", "")
            if (commandMap.containsKey(commandName)) returnValue = commandMap[commandName]!!.handler(methods, privateMethods, method, variablePool, privateVariablePool)
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
            return loadMethod(multiMethod, command, privateMethod.get(command) as ArrayNode, shouldReturn)
        }

        if (privateMethod.has(command)) {
            val multiMethod = MapNode()
            fileMethod.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            return loadMethod(multiMethod, command, privateMethod.get(command) as ArrayNode, shouldReturn)
        }

        if (fileMethod.has(command)) {
            val multiMethod = MapNode()
            fileMethod.obj.forEach(multiMethod::put)
            privateMethod.obj.forEach(multiMethod::put)
            return loadMethod(multiMethod, command, fileMethod.get(command) as ArrayNode, shouldReturn)
        }

        throw JsonProcessorException(command, "Cannot find file in method: $command")
    }

    private fun init() {
        commandMap["Array"]         = CommandArray()
        commandMap["Calculator"]    = Calculator()
        commandMap["Greater"]       = Greater()
        commandMap["Less"]          = Less()
        commandMap["Is"]            = Is()
        commandMap["IsNot"]         = IsNot()
        commandMap["If"]            = If()
        commandMap["Return"]        = Return()
        commandMap["PrivateVar"]    = PrivateVar()
        commandMap["Var"]           = Var()
    }
}