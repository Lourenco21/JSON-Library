import org.junit.Test
import org.junit.Assert.assertEquals

class JValueTests{

    @Test
    fun testJString(){
        assertEquals("\"teste\"", JString("teste").toText())
    }

    @Test
    fun testJBoolean(){
        assertEquals("false", JBoolean(false).toText())
        assertEquals("true", JBoolean(true).toText())
    }

    @Test
    fun testJNumber(){
        assertEquals("1522", JNumber(1522).toText())
        assertEquals("1522.11", JNumber(1522.11).toText())
    }

    @Test
    fun testJNull(){
        assertEquals("null", JNull().toText())
    }
    @Test
    fun testJArray(){
        val array = JArray(listOf(JString("a"), JNumber(1522), JBoolean(true)))
        assertEquals("[\n\"a\",\n1522,\ntrue\n]", array.toText())
    }
    @Test
    fun testJObject(){
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
}""".trimIndent()

        assertEquals(expected, obj.toText())
    }
    @Test
    fun testNestedJson() {
        val obj = JObject(
            listOf(
                JField("data", JArray(
                    listOf(
                        JObject(listOf(
                            JField("id", JNumber(1)),
                            JField("valid", JBoolean(false))
                        )),
                        JObject(listOf(
                            JField("id", JNumber(2)),
                            JField("valid", JBoolean(true))
                        ))
                    )
                ))
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