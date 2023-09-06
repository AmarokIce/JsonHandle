package club.someoneice.jsonprocessor

import club.someoneice.jsonprocessor.multiple.ProcessorEntry
import club.someoneice.jsonprocessor.simple.JsonProcessorSimpleCore
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./JsonTest.json5")

        JsonProcessorSimpleCore(getJson5, file)

        ProcessorEntry(file).get()
    }
}