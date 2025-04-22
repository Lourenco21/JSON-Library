interface JValue {
    fun toText(): String = toText(0)
    fun toText(indentLevel: Int): String
}

class JField(val name: String, val value: JValue)

class JObject(val fields: List<JField>) : JValue {
    override fun toText(indentLevel: Int): String {
        if (fields.isEmpty()) return "{}"

        val indent = "  ".repeat(indentLevel)
        val childIndent = "  ".repeat(indentLevel + 1)

        val content = fields.joinToString(",\n") {
            "$childIndent\"${it.name}\": ${it.value.toText(indentLevel + 1)}"
        }

        return "{\n$content\n$indent}"
    }
}

class JArray(val elements: List<JValue>) : JValue {
    override fun toText(indentLevel: Int): String {
        if (elements.isEmpty()) return "[]"

        val indent = "  ".repeat(indentLevel)
        val childIndent = "  ".repeat(indentLevel + 1)

        val content = elements.joinToString(",\n") {
            "$childIndent${it.toText(indentLevel + 1)}"
        }

        return "[\n$content\n$indent]"
    }
}

class JString(val value: String) : JValue {
    override fun toText(indentLevel: Int): String = "\"$value\""
}

class JBoolean(val value: Boolean) : JValue {
    override fun toText(indentLevel: Int): String = if(value) "true" else "false"
}

class JNumber(val value: Number) : JValue {
    override fun toText(indentLevel: Int): String = value.toString()
}

class JNull : JValue {
    override fun toText(indentLevel: Int): String = "null"
}
