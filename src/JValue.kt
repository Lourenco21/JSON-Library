import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Interface base para todos os tipos de valor JSON suportados (objeto, array, string, número, booleano ou nulo).
 */
interface JValue {
    /**
     * Converte o valor JSON para uma representação em texto formatada.
     * @return Texto JSON formatado sem indentação.
     */
    fun toText(): String = toText(0)

    /**
     * Converte o valor JSON para texto formatado com o nível de indentação especificado.
     * @param indentLevel Nível de indentação a aplicar.
     * @return Representação textual do JSON.
     */
    fun toText(indentLevel: Int): String
}

/**
 * Representa um campo de um objeto JSON, com nome e valor.
 * @property name Nome da chave.
 * @property value Valor associado à chave.
 */
data class JField(val name: String, val value: JValue)

/**
 * Representa um objeto JSON (estrutura key-value).
 * @property fields Lista de campos (pares key-value).
 * @throws IllegalArgumentException se existirem chaves duplicadas.
 */
data class JObject(val fields: List<JField>) : JValue {

    init {
        val names = fields.map { it.name }
        if (names.size != names.toSet().size) {
            throw IllegalArgumentException("Não é permitido usar chaves duplicadas na criação de um JObject!")
        }
    }

    /**
     * Converte o objeto JSON para texto formatado.
     */
    override fun toText(indentLevel: Int): String {
        if (fields.isEmpty()) return "{}"

        val indent = "  ".repeat(indentLevel)
        val childIndent = "  ".repeat(indentLevel + 1)

        val content = fields.joinToString(",\n") {
            "$childIndent\"${it.name}\": ${it.value.toText(indentLevel + 1)}"
        }

        return "{\n$content\n$indent}"
    }

    /**
     * Filtra os campos do objeto de acordo com um predicado fornecido.
     * @param predicate Função que determina se um campo deve ser incluído.
     * @return Novo JObject com apenas os campos filtrados.
     */
    fun filter(predicate: (JField) -> Boolean): JObject {
        return JObject(fields.filter(predicate))
    }
}

/**
 * Representa um array JSON.
 * @property elements Lista de elementos do array.
 */
data class JArray(val elements: List<JValue>) : JValue {

    /**
     * Converte o array JSON para texto formatado.
     */
    override fun toText(indentLevel: Int): String {
        if (elements.isEmpty()) return "[]"

        val indent = "  ".repeat(indentLevel)
        val childIndent = "  ".repeat(indentLevel + 1)

        val content = elements.joinToString(",\n") {
            "$childIndent${it.toText(indentLevel + 1)}"
        }

        return "[\n$content\n$indent]"
    }

    /**
     * Filtra os elementos do array de acordo com um predicado fornecido.
     * @param predicate Função que determina se um elemento deve ser incluído.
     * @return Novo JArray com os elementos filtrados.
     */
    fun filter(predicate: (JValue) -> Boolean): JArray {
        return JArray(elements.filter(predicate))
    }

    /**
     * Transforma os elementos do array usando a função fornecida.
     * @param transform Função de transformação a aplicar a cada elemento.
     * @return Novo JArray com os elementos transformados.
     */
    fun map(transform: (JValue) -> JValue): JArray {
        return JArray(elements.map(transform))
    }
}

/**
 * Representa uma string JSON.
 * @property value Valor textual.
 */
data class JString(val value: String) : JValue {
    override fun toText(indentLevel: Int): String = "\"$value\""
}

/**
 * Representa um valor booleano JSON.
 * @property value true ou false.
 */
data class JBoolean(val value: Boolean) : JValue {
    override fun toText(indentLevel: Int): String = if (value) "true" else "false"
}

/**
 * Representa um número JSON (inteiro ou decimal).
 * @property value Valor numérico.
 */
data class JNumber(val value: Number) : JValue {
    override fun toText(indentLevel: Int): String = value.toString()
}

/**
 * Representa um valor nulo JSON.
 */
object JNull : JValue {
    override fun toText(indentLevel: Int): String = "null"
}

/**
 * Aplica uma função a todos os nós do JSON (recursivamente).
 * @param visitor Função a aplicar a cada valor JSON.
 */
fun JValue.accept(visitor: (JValue) -> Unit) {
    visitor(this)
    when (this) {
        is JObject -> this.fields.forEach { it.value.accept(visitor) }
        is JArray -> this.elements.forEach { it.accept(visitor) }
        else -> {}
    }
}

/**
 * Valida se todas as chaves em todos os objetos JSON são únicas e não estão em branco.
 * @return true se todas as chaves forem válidas, false caso contrário.
 */
fun JValue.validateKeys(): Boolean {
    var valid = true

    this.accept {
        if (it is JObject) {
            it.fields.forEach {
                if (it.name.isBlank()) valid = false
            }
            val names = it.fields.map { it.name }
            val unique = names.toSet()
            if (names.size != unique.size) valid = false
        }
    }
    return valid
}

/**
 * Valida se todos os arrays JSON têm elementos do mesmo tipo (ignorando nulos).
 * @return true se todos os arrays forem homogéneos, false caso contrário.
 */
fun JValue.validateArrayTypes(): Boolean {
    var valid = true

    this.accept {
        if (it is JArray && it.elements.isNotEmpty()) {
            val firstType = it.elements.firstOrNull { element -> element !is JNull }?.javaClass
            if (firstType != null) {
                if (!it.elements.all { element -> element is JNull || element.javaClass == firstType })
                    valid = false
            }
        }
    }
    return valid
}

/**
 * Associa um parâmetro a uma propriedade de uma data class.
 * @param parameter Parâmetro a procurar.
 * @return Propriedade correspondente.
 */
private fun KClass<*>.matchProperty(parameter: KParameter): KProperty<*> {
    require(isData)
    return declaredMemberProperties.first { it.name == parameter.name }
}

/**
 * Converte um objeto Kotlin para o seu equivalente JSON.
 * Suporta tipos primitivos, listas, mapas e data classes.
 * @param value Valor a converter.
 * @return Representação JSON como um JValue.
 */
fun instantiateJsonModel(value: Any?): JValue {
    return when (value) {
        is Int, is Double -> JNumber(value)
        is Boolean -> JBoolean(value)
        is String -> JString(value)
        null -> JNull
        is List<*> -> JArray(value.map { instantiateJsonModel(it) })
        is Enum<*> -> JString(value.name)
        is Map<*, *> -> {
            if (value.keys.any { it !is String }) {
                throw IllegalArgumentException("O nome do campo tem de ser uma String!")
            }
            val fields = value.entries.map { (k, v) -> JField(k as String, instantiateJsonModel(v)) }
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
