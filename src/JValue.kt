interface JValue {
    fun toText(): String
}

class JField(val name: String, val value: JValue)

class JObject(val values: List<JField>) : JValue {
    override fun toText(): String = values.joinToString(separator = ",\n", prefix = "{\n", postfix = "\n}"){
        "\"${ it.name }\": ${it.value.toText()}"
    }
}

class JArray(val elements: List<JValue>) : JValue {
    override fun toText(): String = elements.joinToString(separator = ",", prefix = "[", postfix = "]")
}

class JString(val value: String) : JValue {
    override fun toText(): String = "\"" + value + "\""
}

class JBoolean(val value: Boolean) : JValue {
    override fun toText(): String = if(value) "true" else "false"
}

class JNumber(val value: Number) : JValue {
    override fun toText(): String = value.toString()
}

class JNull() : JValue {
    override fun toText(): String = ""
}
