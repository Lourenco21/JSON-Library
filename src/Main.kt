fun main() {
    println(JString("AAAAAAAAAA").toText())
    println(JBoolean(true).toText())
    println(JBoolean(false).toText())
    val json = JObject(
        listOf(
            JField("name", JString("Alice")),
            JField("age", JNumber(30)),
            JField("isStudent", JBoolean(false)),
            JField("address", JObject(
                listOf(
                    JField("city", JString("Paris")),
                    JField("zip", JNumber(75000))
                )
            )),
            JField("hobbies", JArray(listOf(JString("reading"), JString("cycling")))),
            JField("extra", JNull())
        )
    )

    println(json.toText())
}