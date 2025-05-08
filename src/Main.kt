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
            JField("extra", JNull)
        )
    )

    println(json.toText())

    val course = Course(
        "PA", 6, listOf(
            EvalItem("quizzes", .2, false, null),
            EvalItem("project", .8, true, EvalType.PROJECT)
        )
    )


    println(instantiateJsonModel(course).toText())
}
data class Course(
    val name: String,
    val credits: Int,
    val evaluation: List<EvalItem>
)


data class EvalItem(
    val name: String,
    val percentage: Double,
    val mandatory: Boolean,
    val type: EvalType?
)


enum class EvalType {
    TEST, PROJECT, EXAM
}

