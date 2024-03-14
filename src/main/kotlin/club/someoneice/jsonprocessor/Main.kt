package club.someoneice.jsonprocessor

import club.someoneice.jsonprocessor.core.FileManager
import java.io.File

fun main(vararg args: String) {
    val root = if (args.isEmpty()) {
        println("Key the root directory:")
        readln()
    } else args[0]

    FileManager.scanFile(File(root))

}