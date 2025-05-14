import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val endpoint: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path()

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param()


class GetJson(vararg val controllers: KClass<*>){

    fun getPaths() : Map<String, Pair<String, Any>>{
        val paths = mutableMapOf<String, Pair<String,Any>>()


        controllers.forEach{ c ->
            val controllerInstance = c.createInstance()
            var path = "/" + c.findAnnotation<Mapping>()?.endpoint.toString() + "/"
            c.functions.forEach {
                if(it.hasAnnotation<Mapping>()){
                    paths.put(path + it.findAnnotation<Mapping>()?.endpoint.toString(),  Pair(it.name, controllerInstance))
                }
            }
        }

        return paths
    }

    fun run(path : String) : JValue{
        val paths = getPaths()
        var result : Any? = null

        paths.forEach {
            val functionToBeCalled = it.value.first
            val controllerInstance = it.value.second
            if(it.key == path){
                val function = controllerInstance::class.functions.first { f -> f.name == functionToBeCalled }
                result = function.call(controllerInstance)
            }
        }
        return instantiateJsonModel(result)
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
    println(app.getPaths())
    println(app.run("/api/ints"))
    println(app.run("/api/ints").toText())
}



