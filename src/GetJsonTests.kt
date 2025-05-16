import org.junit.Test
import org.junit.Assert.*
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

@Mapping("api")
class TestController {

    @Mapping("ints")
    fun getInts(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun getPair(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun getPathVar(@Path pathvar: String): String = pathvar + "!"

    @Mapping("args")
    fun getArgs(@Param n: Int, @Param text: String): Map<String, String> =
        mapOf(text to text.repeat(n))
}

class GetJsonTests {

    @Test
    fun testDemoEndpointReturnsListOfInts() {
        val app = GetJson(TestController::class)
        val result = app.run("/api/ints")
        val expected = JArray(listOf(JNumber(1), JNumber(2), JNumber(3)))
        assertEquals(expected, result)
    }

    @Test
    fun testPairEndpointReturnsCorrectObject() {
        val app = GetJson(TestController::class)
        val result = app.run("/api/pair")
        val expected = JObject(
            listOf(
                JField("first", JString("um")),
                JField("second", JString("dois"))
            )
        )
        assertEquals(expected.toText(), result.toText())
    }

    @Test
    fun testPathVariableIsCorrectlyInjected() {
        val app = GetJson(TestController::class)
        val result = app.run("/api/path/ola")
        assertEquals(JString("ola!"), result)
    }

    @Test
    fun testQueryParametersAreCorrectlyInjected() {
        val app = GetJson(TestController::class)
        val result = app.run("/api/args?n=2&text=PA")
        val expected = JObject(listOf(JField("PA", JString("PAPA"))))
        assertEquals(expected, result)
    }

    @Test
    fun testExceptionThrown() {
        assertThrows(IllegalArgumentException::class.java) {
            throw IllegalArgumentException("Erro de teste")
        }
    }

    @Test
    fun testHttpServerReturnsExpectedJson() {
        val app = GetJson(TestController::class)

        thread {
            app.startServer(8082)
        }

        Thread.sleep(500)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://localhost:8082/api/ints")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body()?.string()
            val expected = JArray(listOf(JNumber(1), JNumber(2), JNumber(3))).toText()
            assertEquals(expected, body)
        }
    }
}

