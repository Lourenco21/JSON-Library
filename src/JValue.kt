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

    fun filter(predicate: (JField) -> Boolean): JObject {
        return JObject(fields.filter(predicate))
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

    fun filter(predicate: (JValue) -> Boolean): JArray {
        return JArray(elements.filter(predicate))
    }

    fun map(transform: (JValue) -> JValue): JArray {
        return JArray(elements.map(transform))
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

fun JValue.accept(visitor:(JValue) -> Unit){
    visitor(this)
    when(this){
        is JObject -> this.fields.forEach { it.value.accept(visitor) }
        is JArray -> this.elements.forEach { it.accept(visitor) }
        else -> {}
    }
}

fun JValue.validateKeys(): Boolean{
    var valid = true

    this.accept{
        if(it is JObject){
            it.fields.forEach {
                if(it.name.isBlank())
                    valid = false
            }
            val names = it.fields.map { it.name }
            val unique = names.toSet()
            if(names.size != unique.size)
                valid = false
        }
    }
    return valid
}

fun JValue.validateArrayTypes(): Boolean{

    var valid = true

    this.accept{
        if(it is JArray && it.elements.isNotEmpty()){
            val firstType = it.elements.firstOrNull(){ element -> element !is JNull}?.javaClass
            if(firstType != null){
                if(!it.elements.all{element -> element is JNull || element.javaClass == firstType})
                    valid = false
            }
        }
    }
    return valid
}
