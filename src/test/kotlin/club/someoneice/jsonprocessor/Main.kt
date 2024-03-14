package club.someoneice.jsonprocessor

import club.someoneice.jsonprocessor.core.FileManager
import club.someoneice.jsonprocessor.core.ProcessorCore
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        FileManager.scanFile(File("./json/src"))
        ProcessorCore.main()
    }
}