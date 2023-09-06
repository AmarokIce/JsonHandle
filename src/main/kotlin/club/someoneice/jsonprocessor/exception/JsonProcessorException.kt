package club.someoneice.jsonprocessor.exception

open class JsonProcessorException(protected val fileName: String, protected val msg: String): Throwable() {
    init {
        printStackTrace()
    }

    override fun printStackTrace() {
        println("${this.javaClass.name}: $msg")
        println("From: $fileName")
    }
}

class NonVariableInPoolException(fileName: String = "Unknown", msg: String): JsonProcessorException(fileName, msg)
class NonMainMethodException(fileName: String = "Unknown", msg: String): JsonProcessorException(fileName, msg)