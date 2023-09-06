package club.someoneice.jsonprocessor.multiple

import club.someoneice.json.node.ArrayNode
import club.someoneice.json.node.BooleanNode
import club.someoneice.json.node.StringNode
import club.someoneice.jsonprocessor.json


class JsonProcessorMixin internal constructor(private val core: JsonProcessorCore) {
    internal fun mixin(command: StringNode, pMethodMixin: StringNode, pMethodOriginal: StringNode): BooleanNode {
        if (pMethodMixin.obj[0] != '!' || pMethodOriginal.obj[0] != '!') return BooleanNode(false)

        fun findMethod(name: String): ArrayNode {
            val fileName = name.substring(1, name.indexOf("#"))
            val methodName = name.substring(name.indexOf("#") + 1)

            return core.fileManager.getMethod(fileName, methodName)
        }

        val methodMixin = findMethod(pMethodMixin.obj)
        val methodOriginal = findMethod(pMethodOriginal.obj)

        fun access(): BooleanNode {
            val array = ArrayNode()
            array.addAll(methodMixin.obj)
            array.addAll(methodOriginal.obj)

            val fileName = pMethodOriginal.obj.substring(1, pMethodOriginal.obj.indexOf("#"))
            val methodName = pMethodOriginal.obj.substring(pMethodOriginal.obj.indexOf("#") + 1)

            core.fileManager.fileMap[fileName]?.let {
                it.obj[methodName] = array
                return BooleanNode(true)
            }

            return BooleanNode(false)
        }

        fun incise(): BooleanNode {
            val array = ArrayNode()
            for (i in 0 until methodOriginal.obj.size) {
                if (i == methodOriginal.obj.size && json.tryPullArrayOrEmpty(methodOriginal[i])[0].toString().lowercase() != "@return") {
                    array.add(json.tryPullArrayOrEmpty(methodOriginal[i]))
                    array.addAll(methodMixin.obj)
                }

                if (json.tryPullArrayOrEmpty(methodOriginal[i])[0].toString().lowercase() == "@return") {
                    array.addAll(methodMixin.obj)
                    array.add(json.tryPullArrayOrEmpty(methodOriginal[i]))
                } else array.add(json.tryPullArrayOrEmpty(methodOriginal[i]))
            }

            val fileName = pMethodOriginal.obj.substring(1, pMethodOriginal.obj.indexOf("#"))
            val methodName = pMethodOriginal.obj.substring(pMethodOriginal.obj.indexOf("#") + 1)

            core.fileManager.fileMap[fileName]?.let {
                it.obj[methodName] = array
                return BooleanNode(true)
            }

            return BooleanNode(false)
        }

        fun override(): BooleanNode {
            val fileName = pMethodOriginal.obj.substring(1, pMethodOriginal.obj.indexOf("#"))
            val methodName = pMethodOriginal.obj.substring(pMethodOriginal.obj.indexOf("#") + 1)

            core.fileManager.fileMap[fileName]?.let {
                it.obj[methodName] = methodMixin
                return BooleanNode(true)
            }

            return BooleanNode(false)
        }

        return when (command.obj.lowercase()) {
            "@access"   -> access()
            "@incise"   -> incise()
            "@override" -> override()
            else        -> BooleanNode(false)
        }
    }
}