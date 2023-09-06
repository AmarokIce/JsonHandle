package club.someoneice.jsonprocessor

import club.someoneice.json.JSON

fun Boolean.goTrue(apply: () -> Unit): Boolean {
    if (this) apply()
    return this
}

fun Boolean.goFalse(apply: () -> Unit): Boolean {
    if (!this) apply()
    return this
}

val getJson: JSON = JSON.json
val getJson5: JSON = JSON.json5

val json = getJson5