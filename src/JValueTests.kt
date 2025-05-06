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
}

class JValueFilterTests {

    @Test
    fun testJObjectFilterByKeyName() {
        val obj = JObject(
            listOf(
                JField("id", JNumber(1)),
                JField("name", JString("Guilherme")),
                JField("admin", JBoolean(true))
            )
        )
        val filtered = obj.filter { it.name != "admin" }

        val expected = JObject(
            listOf(
                JField("id", JNumber(1)),
                JField("name", JString("Guilherme"))
            )
        )

        assertEquals(expected.toText(), filtered.toText())
    }
    @Test
    fun testJObjectFilterByValueType() {
        val obj = JObject(
            listOf(
                JField("username", JString("Lourenço21")),
                JField("age", JNumber(30)),
                JField("active", JBoolean(false))
            )
        )
        val filtered = obj.filter { it.value is JString }

        val expected = JObject(
            listOf(
                JField("username", JString("Lourenço21"))
            )
        )

        assertEquals(expected.toText(), filtered.toText())
    }
    @Test
    fun testJArrayFilterNumbersOnly() {
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
    fun testJArrayFilterRemoveNulls() {
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
    fun testJArrayFilterNestedObjects() {
        val arr = JArray(
            listOf(
                JObject(listOf(JField("type", JString("student")))),
                JObject(listOf(JField("type", JString("admin")))),
                JObject(listOf(JField("type", JString("guest"))))
            )
        )
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
    @Test
    fun testValidateKeysValidObject() {
        val obj = JObject(
            listOf(
                JField("name", JString("Guilherme")),
                JField("age", JNumber(21)),
                JField("active", JBoolean(true))
            )
        )
        assertTrue(obj.validateKeys())
    }

    @Test
    fun testValidateKeysDuplicateKeys() {
        val obj = JObject(
            listOf(
                JField("name", JString("Lourenço")),
                JField("name", JString("Duplicate"))
            )
        )
        assertFalse(obj.validateKeys())
    }

    @Test
    fun testValidateKeysBlankKey() {
        val obj = JObject(
            listOf(
                JField("", JString("blank")),
                JField("valid", JString("ok"))
            )
        )
        assertFalse(obj.validateKeys())
    }

    @Test
    fun testValidateKeysNestedObjects() {
        val obj = JObject(
            listOf(
                JField("user", JObject(
                    listOf(
                        JField("id", JNumber(1)),
                        JField("name", JString("Guilherme")),
                        JField("name", JString("Duplicate"))
                    )
                ))
            )
        )
        assertFalse(obj.validateKeys())
    }
    @Test
    fun testValidateArrayTypesAllSameType() {
        val arr = JArray(
            listOf(JString("a"), JString("b"), JString("c"))
        )
        assertTrue(arr.validateArrayTypes())
    }

    @Test
    fun testValidateArrayTypesMixedTypes() {
        val arr = JArray(
            listOf(JString("a"), JNumber(1), JString("b"))
        )
        assertFalse(arr.validateArrayTypes())
    }

    @Test
    fun testValidateArrayTypesWithNullsOnly() {
        val arr = JArray(
            listOf(JNull(), JNull())
        )
        assertTrue(arr.validateArrayTypes())
    }

    @Test
    fun testValidateArrayTypesSameTypeWithNulls() {
        val arr = JArray(
            listOf(JNumber(1), JNull(), JNumber(2))
        )
        assertTrue(arr.validateArrayTypes())
    }

    @Test
    fun testValidateArrayTypesDifferentTypesWithNulls() {
        val arr = JArray(
            listOf(JString("a"), JNull(), JNumber(2))
        )
        assertFalse(arr.validateArrayTypes())
    }

    @Test
    fun testValidateArrayTypesNestedArrays() {
        val json = JObject(
            listOf(
                JField("numbers", JArray(listOf(JNumber(1), JNumber(2)))),
                JField("mixed", JArray(listOf(JString("x"), JNumber(3))))
            )
        )
        assertFalse(json.validateArrayTypes())
    }
}

class JArrayMapTests{

    @Test
    fun testMapToUpperCase() {
        val arr = JArray(
            listOf(JString("maça"), JString("banana"), JString("limão"))
        )
        val transformed = arr.map {
            if (it is JString) JString(it.value.uppercase()) else it
        }
        val expected = JArray(
            listOf(JString("MAÇA"), JString("BANANA"), JString("LIMÃO"))
        )
        assertEquals(expected.toText(), transformed.toText())
    }

    @Test
    fun testMapEmptyArray() {
        val arr = JArray(emptyList())

        val transformed = arr.map { it }

        val expected = JArray(emptyList())

        assertEquals(expected.toText(), transformed.toText())
    }

    @Test
    fun testMapWithNull() {
        val arr = JArray(
            listOf(JString("maça"), JNull(), JNumber(27))
        )
        val transformed = arr.map {
            when (it) {
                is JString -> JString(it.value.uppercase())
                is JNumber -> JNumber(it.value.toInt() + 1)
                else -> it
            }
        }

        val expected = JArray(
            listOf(JString("MAÇA"), JNull(), JNumber(28))
        )

        assertEquals(expected.toText(), transformed.toText())
    }

    @Test
    fun testMapNestedObjects() {
        val arr = JArray(
            listOf(
                JObject(listOf(JField("name", JString("Lourenço")), JField("age", JNumber(21)))),
                JObject(listOf(JField("name", JString("Guilherme")), JField("age", JNumber(21))))
            )
        )
        val transformed = arr.map {
            if (it is JObject) {
                JObject(
                    it.fields.map { field ->
                        if (field.name == "age" && field.value is JNumber) {
                            JField(field.name, JNumber(field.value.value.toInt() + 1))
                        } else {
                            field
                        }
                    }
                )
            } else {
                it
            }
        }

        val expected = JArray(
            listOf(
                JObject(listOf(JField("name", JString("Lourenço")), JField("age", JNumber(22)))),
                JObject(listOf(JField("name", JString("Guilherme")), JField("age", JNumber(22))))
            )
        )

        assertEquals(expected.toText(), transformed.toText())
    }

    @Test
    fun testMapTransformDifferentTypes() {
        val arr = JArray(
            listOf(JString("maça"), JNumber(10), JBoolean(true))
        )

        val transformed = arr.map {
            when (it) {
                is JString -> JString(it.value.uppercase())
                is JNumber -> JNumber(it.value.toInt() + 1)
                is JBoolean -> JBoolean(!it.value)
                else -> it
            }
        }

        val expected = JArray(
            listOf(JString("MAÇA"), JNumber(11), JBoolean(false))
        )

        assertEquals(expected.toText(), transformed.toText())
    }

}