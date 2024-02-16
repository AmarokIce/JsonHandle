package club.someoneice.jsonprocessor.multiple

object CalculatorV2 {
    fun calculator(str: String): Double {
        val newStr = str.replace(" ", "")

        val steps: ArrayList<Double> = ArrayList()
        val numberBuffer = StringBuilder()
        val advancedBuffer = StringBuilder()

        var isArray = false

        fun checkCalculator(c: Char) {
            if (advancedBuffer.isEmpty()) {
                if (numberBuffer.isEmpty()) throw CalculatorException("Unexpected symbols: $c in formula $str")
                steps.add(numberBuffer.toString().toDouble())
                numberBuffer.clear()
                return
            }

            val symbolSuf = advancedBuffer[advancedBuffer.length - 1]
            val symbolPre = advancedBuffer[0]
            var strReplace = advancedBuffer.removeSuffix(symbolSuf.toString()).toString()
            if (symbolPre == '-') strReplace = strReplace.removePrefix(symbolPre.toString())
            val numberA = strReplace.toDouble()
            val numberB = numberBuffer.toString().toDouble()
            var callback: Double = when (symbolSuf) {
                '*' -> numberA * numberB
                '/' -> numberA / numberB
                else -> throw CalculatorException("Unexpected symbols: $c in formula $str")
            }
            numberBuffer.clear()
            advancedBuffer.clear()
            if (symbolPre == '-') callback *= -1
            if (c == '*' || c == '/') advancedBuffer.append(callback).append(c)
            else steps.add(callback)
        }

        fun subtractHandler(c: Char) {
            if (numberBuffer.isNotEmpty()) checkCalculator(c)
            numberBuffer.append(c)
        }

        fun advancedHandler(c: Char) {
            if (advancedBuffer.isEmpty()) {
                advancedBuffer.append(numberBuffer.toString()).append(c)
                numberBuffer.clear()
            }
            else checkCalculator(c)
        }

        fun handlerArray(c: Char) {
            if (c == ')') {
                val start = numberBuffer.length - numberBuffer.toString().replace("(","").length
                val end = numberBuffer.length - numberBuffer.toString().replace(")","").length
                val count = start - end
                if (count < 0) throw CalculatorException("Unexpected symbols: $c in formula $str")
                if (count == 0) {
                    val strArray = numberBuffer.toString();
                    numberBuffer.clear().append(calculator(strArray))
                    isArray = false
                } else numberBuffer.append(c)
            } else numberBuffer.append(c)
        }

        fun startArray(c: Char) {
            if (numberBuffer.isNotEmpty()) advancedHandler(c)
            isArray = true
        }

        for (c in newStr.indices) {
            val char = newStr[c]

            if (isArray) handlerArray(char)
            else when (char) {
                '(' -> startArray(char)
                '*', '/' -> advancedHandler(char)
                '+' -> checkCalculator(char)
                '-' -> subtractHandler(char)
                else -> numberBuffer.append(char)
            }
        }
        checkCalculator('=')
        var number = 0.0
        steps.forEach {
            number += it
        }
        return number
    }

    class CalculatorException(cause: String): Exception(cause)
}