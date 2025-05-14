import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val endpoint: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path()

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param()


class GetJson(vararg val controllers: KClass<*>) {

    fun getPaths(): Map<String, Pair<String, Any>> {
        val paths = mutableMapOf<String, Pair<String, Any>>()

        controllers.forEach { c ->
            val controllerInstance = c.createInstance()
            var path = "/" + c.findAnnotation<Mapping>()?.endpoint.toString() + "/"
            c.functions.forEach {
                if (it.hasAnnotation<Mapping>()) {
                    path += it.findAnnotation<Mapping>()?.endpoint.toString()

                }

                paths.put(path, Pair(it.name, controllerInstance))
                path = "/" + c.findAnnotation<Mapping>()?.endpoint.toString() + "/"
            }
        }

        return paths
    }

    fun run(path: String): JValue {
        val paths = getPaths()
        var result: Any? = null
        var realPath = path
        if (path.contains("?")) {
            realPath = path.split("?")[0]
        }
        paths.forEach {
            val functionToBeCalled = it.value.first
            val controllerInstance = it.value.second
            if (it.key == realPath) {
                val function = controllerInstance::class.functions.first { f -> f.name == functionToBeCalled }
                val realParams = function.parameters.filter { it.kind == KParameter.Kind.VALUE }
                if (realParams.isNotEmpty()) {
                    var args = mutableMapOf<KParameter, Any?>()
                    args[function.instanceParameter!!] = controllerInstance
                    function.parameters.forEach {
                        if (it.hasAnnotation<Param>()) {
                            args[it] = splitArgs(path, it)
                        }
                    }
                    result = function.callBy(args)
                } else {
                    result = function.call(controllerInstance)
                }

            }
        }
        return instantiateJsonModel(result)
    }

    fun mapType(value: String, type: KType): Any =
        when (type.classifier) {
            Int::class -> value.toInt()
            Double::class -> value.toDouble()
            String::class -> value
            Boolean::class -> value.toBoolean()
            else -> TODO("unsupported: " + type.classifier.toString())
        }

    fun splitArgs(path: String, param: KParameter): Any? {
        var arg : Any? = null
        val pathParts = path.split('?')
        val argsParts = pathParts[1].split("&")
        for (part in argsParts) {
            val keyValue = part.split("=")
            if (param.name == keyValue[0]) {
                 arg = mapType(keyValue[1], param.type)
            }
        }
        return arg
    }
}

@Mapping("api")
class Controller {

    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun path(
        @Path pathvar: String
    ): String = "$pathvar!"

    @Mapping("args")
    fun args(
        @Param n: Int,
        @Param text: String
    ): Map<String, String> = mapOf(text to text.repeat(n))
}

fun main() {
    val app = GetJson(Controller::class)
    println(app.getPaths().keys)
    println(app.run("/api/args?n=3&text=PA"))
    println(app.run("/api/args?n=3&text=PA").toText())
}



