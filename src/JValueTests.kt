import org.junit.Test
import org.junit.Assert.*

class JValueTests {

    @Test
    fun testJString() {
        assertEquals("\"teste\"", JString("teste").toText())
    }

    @Test
    fun testJBoolean() {
        assertEquals("false", JBoolean(false).toText())
        assertEquals("true", JBoolean(true).toText())
    }

    @Test
    fun testJNumber() {
        assertEquals("1522", JNumber(1522).toText())
        assertEquals("1522.11", JNumber(1522.11).toText())
    }

    @Test
    fun testJNull() {
        assertEquals("null", JNull().toText())
    }

    @Test
    fun testJArray() {
        val array = JArray(listOf(JString("a"), JNumber(1522), JBoolean(true)))
        assertEquals("[\n  \"a\",\n  1522,\n  true\n]", array.toText())
    }

    @Test
    fun testJObject() {
        val obj = JObject(
            listOf(
                JField("name", JString("Json")),
                JField("active", JBoolean(true)),
                JField("age", JNumber(24)),
                JField("extra", JNull())
            )
        )
        val expected = """{
  "name": "Json",
  "active": true,
  "age": 24,
  "extra": null
}"""

        assertEquals(expected, obj.toText())
    }

    @Test
    fun testNestedJson() {
        val obj = JObject(
            listOf(
                JField(
                    "data", JArray(
                        listOf(
                            JObject(
                                listOf(
                                    JField("id", JNumber(1)),
                                    JField("valid", JBoolean(false))
                                )
                            ),
                            JObject(
                                listOf(
                                    JField("id", JNumber(2)),
                                    JField("valid", JBoolean(true))
                                )
                            )
                        )
                    )
                )
            )
        )
        val expected = """{
  "data": [
    {
      "id": 1,
      "valid": false
    },
    {
      "id": 2,
      "valid": true
    }
  ]
}""".trimIndent()

        assertEquals(expected, obj.toText())
    }

//TESTES PARA FILTROS
}
class JValueFilterTests {

    @Test
    fun testJObjectFilter_byKeyName() {
        val obj = JObject(
            listOf(
                JField("id", JNumber(1)),
                JField("name", JString("Alice")),
                JField("admin", JBoolean(true))
            )
        )
        val filtered = obj.filter { it.name != "admin" }

        val expected = JObject(
            listOf(
                JField("id", JNumber(1)),
                JField("name", JString("Alice"))
            )
        )

        assertEquals(expected.toText(), filtered.toText())
    }
    @Test
    fun testJObjectFilter_byValueType() {
        val obj = JObject(
            listOf(
                JField("username", JString("user123")),
                JField("age", JNumber(30)),
                JField("active", JBoolean(false))
            )
        )
        // Keep only fields whose value is a string
        val filtered = obj.filter { it.value is JString }

        val expected = JObject(
            listOf(
                JField("username", JString("user123"))
            )
        )

        assertEquals(expected.toText(), filtered.toText())
    }
    @Test
    fun testJArrayFilter_numbersOnly() {
        val arr = JArray(
            listOf(JNumber(10), JString("hello"), JNumber(42), JNull())
        )
        val filtered = arr.filter { it is JNumber }

        val expected = JArray(
            listOf(JNumber(10), JNumber(42))
        )

        assertEquals(expected.toText(), filtered.toText())
    }

    @Test
    fun testJArrayFilter_removeNulls() {
        val arr = JArray(
            listOf(JString("one"), JNull(), JString("two"), JNull())
        )
        val filtered = arr.filter { it !is JNull }

        val expected = JArray(
            listOf(JString("one"), JString("two"))
        )

        assertEquals(expected.toText(), filtered.toText())
    }

    @Test
    fun testJArrayFilter_nestedObjects() {
        val arr = JArray(
            listOf(
                JObject(listOf(JField("type", JString("student")))),
                JObject(listOf(JField("type", JString("admin")))),
                JObject(listOf(JField("type", JString("guest"))))
            )
        )

        // Keep only objects where type == "admin"
        val filtered = arr.filter { value ->
            value is JObject && value.fields.any { it.name == "type" && (it.value as? JString)?.value == "admin" }
        }

        val expected = JArray(
            listOf(
                JObject(listOf(JField("type", JString("admin"))))
            )
        )

        assertEquals(expected.toText(), filtered.toText())
    }

}

class TestVisitors{

}