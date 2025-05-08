import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

interface JValue {
    fun toText(): String = toText(0)
    fun toText(indentLevel: Int): String
}

data class JField(val name: String, val value: JValue)

data class JObject(val fields: List<JField>) : JValue {

    init{
        val names = fields.map { it.name }
        if(names.size != names.toSet().size){
            throw IllegalArgumentException("Não é permitido usar chaves duplicadas na criação de um JObject!")
        }
    }

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

data class JArray(val elements: List<JValue>) : JValue {
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

data class JString(val value: String) : JValue {
    override fun toText(indentLevel: Int): String = "\"$value\""
}

data class JBoolean(val value: Boolean) : JValue {
    override fun toText(indentLevel: Int): String = if(value) "true" else "false"
}

data class JNumber(val value: Number) : JValue {
    override fun toText(indentLevel: Int): String = value.toString()
}

object JNull : JValue {
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
            val firstType = it.elements.firstOrNull{ element -> element !is JNull}?.javaClass
            if(firstType != null){
                if(!it.elements.all{element -> element is JNull || element.javaClass == firstType})
                    valid = false
            }
        }
    }
    return valid
}

private fun KClass<*>.matchProperty(parameter: KParameter) : KProperty<*> {
    require(isData)
    return declaredMemberProperties.first { it.name == parameter.name }
}

fun instantiateJsonModel(value: Any?) : JValue{
    return when(value){
        is Int, is Double-> JNumber(value)
        is Boolean -> JBoolean(value)
        is String -> JString(value)
        null -> JNull
        is List<*> -> JArray(value.map { instantiateJsonModel(it) })
        is Enum<*> -> JString(value.name)
        is Map<*, *> -> {
            if(value.keys.any {it !is String}){
                throw IllegalArgumentException("O nome do campo tem de ser uma String!")
            }
            val fields = value.entries.map{ (k, v) -> JField(k as String, instantiateJsonModel(v)) }
            JObject(fields)
        }
        else -> {
            val clazz = value::class
            require(clazz.isData)
            val fields = mutableListOf<JField>()
            clazz.primaryConstructor?.parameters?.forEach { p ->
                val prop = clazz.matchProperty(p)
                fields.add(JField(prop.name, instantiateJsonModel(prop.call(value))))
            }
            JObject(fields)
        }
    }
}
