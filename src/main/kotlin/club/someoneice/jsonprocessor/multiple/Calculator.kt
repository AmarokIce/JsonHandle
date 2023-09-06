package club.someoneice.jsonprocessor.multiple

import kotlin.math.floor

object Calculator {
    fun calculator(str: String, simple: Boolean = false): Int {
        val steps: ArrayList<IntData> = ArrayList()
        var skip = false

        var tit = ""
        var num = ""
        var size = 0
        if (!simple) {
            for (i in str.indices) {
                if (i < size) continue
                when (str[i].toString()) {
                    "+", "-" -> {
                        if (i != 0) {
                            steps.add(IntData(tit, IntObject(num)))
                        }
                        tit = str[i].toString()
                        num = ""
                        skip = false
                    }

                    "*", "/" -> {
                        if (str[i - 1].toString() == ")") {
                            var j = i
                            j += 1
                            val obj = arrayAsTopCon(str, j, num)
                            size = obj.getSize() + j
                            tit = str[i].toString()
                            steps.add(IntData(tit, obj))
                        } else {
                            val obj = arrayAsTopCon(str, i, num)
                            size = obj.getSize() + i
                            steps.add(IntData(tit, obj))
                        }
                        num = ""
                        skip = true
                    }

                    "(" -> {
                        val obj = arrayAsIntObject(str, i)
                        size = obj.getSize() + i
                        steps.add(IntData(tit, obj))
                        skip = true
                    }

                    else -> {
                        num += str[i].toString()
                    }
                }
            }
        } else {
            for (i in str.indices) {
                if (i < size) continue
                when (str[i].toString()) {
                    "+", "-", "*", "/" -> {
                        if (i != 0) {
                            steps.add(IntData(tit, IntObject(num)))
                        }
                        tit = str[i].toString()
                        num = ""
                        skip = false
                    }

                    "(" -> {
                        val obj = arrayAsIntObject(str, i)
                        size = obj.getSize() + i
                        steps.add(IntData(tit, obj))
                        skip = true
                    }

                    else -> {
                        num += str[i].toString()
                    }
                }
            }
        }

        if (!skip) steps.add(IntData(tit, IntObject(num)))

        var nu = 0
        for (key in steps) {
            when (key.path) {
                "+" -> nu += key.int.getInt()
                "-" -> nu -= key.int.getInt()
                "*" -> nu *= key.int.getInt()
                "/" -> nu = floor((nu.toDouble() / key.int.getInt())).toInt()
                else -> nu += key.int.getInt()
            }
        }

        return nu
    }

    private fun arrayAsTopCon(str: String, step: Int, num: String): IntObject {
        var formula = ""
        formula += num
        var size = 0

        for (i in step until str.length) {
            when (str[i].toString()) {
                "(" -> size += 1
                ")" -> size -= 1
                "+", "-" -> if (size == 0) break
            }
            formula += str[i].toString()
        }

        return IntObject(formula)
    }

    private fun arrayAsIntObject(str: String, step: Int): IntObject {
        var formula = ""
        var nu = 0

        for (i in step until str.length) {
            formula += str[i]
            if (str[i].toString() == "(") nu += 1
            if (str[i].toString() == ")") nu -= 1
            if (nu == 0) break
        }

        return IntObject(formula)
    }

    internal data class IntData(
        val path: String,
        val int: IntObject
    )

    internal class IntObject(private val intStr: String) {
        fun getInt(): Int {
            return try {
                if (intStr == "") 0 else intStr.toInt()
            } catch (e: NumberFormatException) {
                if (intStr[0].toString() == "(") {
                    var str = intStr

                    str = str.replaceFirst("(", "")
                    str = str.replaceRange(str.lastIndex, str.length, "")

                    calculator(str, false)
                } else calculator(intStr, true)
            }
        }

        fun getSize(): Int = this.intStr.length
    }
}