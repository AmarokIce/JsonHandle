package club.someoneice.jsonprocessor.util

import club.someoneice.json.JSON

fun Boolean.goTrue(apply: () -> Unit): Boolean {
    if (this) apply()
    return this
}

fun Boolean.goFalse(apply: () -> Unit): Boolean {
    if (!this) apply()
    return this
}

val Json: JSON = JSON.json5