package club.someoneice.jsonprocessor

import club.someoneice.jsonprocessor.multiple.ProcessorEntry
import club.someoneice.jsonprocessor.simple.SimpleJsonProcessorCore
import com.google.common.base.Stopwatch
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./JsonTest.json5")

        println("Simple Processor Start")
        val watch = Stopwatch.createStarted()
        SimpleJsonProcessorCore(getJson5, file)
        println(watch)
        watch.stop()
        println("Simple Processor End \n")

        println("Multiple Processor Start")
        watch.reset().start()
        ProcessorEntry(file).get().runMain()
        println(watch)
        watch.stop()
        println("Multiple Processor End \n")

        val file2 = File("./JsonTest", "JsonTestMain.json5")

        println("File Asker Path Start")
        watch.reset().start()
        ProcessorEntry(file2, file2.parentFile).get().runMain()
        println(watch)
        watch.stop()
        println("Part End")
    }
}