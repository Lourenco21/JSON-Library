<h1>Json Model Library</h1>

A Kotlin library to represent, manipulate, and serialize JSON values programmatically.</br>
It allows developers to build JSON models directly in code, apply functional transformations (map, filter), validate structures, and even infer JSON from Kotlin objects using reflection.

<h2>Requirements</h2>

- Kotlin Standard Library
- [kotlin-reflect](https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect) must be added to your project dependencies.

<h2>Installation</h2>

1. Download the `JSON-Library-1.0.0.jar` file (note that the numbers on the file name, may change according to current version name)
2. Add the `JSON-Library-1.0.0.jar` file to your project dependencies  
   If you are using IntelliJ:  
   `File > Project Structure > Modules > Dependencies > + > JARs or directories`

<h2>Compatibility with the following Kotlin types:</h2>

`Int`</br>
`Double`</br>
`Boolean`</br>
`String`</br>
`List<supported type>`</br>
`Enums`</br>
`null`</br>
`data classes whose properties are of supported types`</br>
`Map<String, T> where T is a supported type`</br>

<h2>Features</h2>

- Representation of all JSON types (`object`, `array`, `string`, `number`, `boolean`, `null`)
- Serialization to valid JSON `String`
- Functional manipulation:
  - `filter` on objects and arrays
  - `map` on arrays
- Support for the Visitor Pattern to enable extensions like validation
- Inference of JSON from Kotlin objects:
  - `data class`
  - `List`, `Map`, `Enum`, `null`, `Int`, `Double`, `Boolean`, `String`
 
<h2>How to Use</h2>

<h3>JSON representation</h3>

The `JValue` interface represents a JSON generic value. All of the following classes implement JValue:

- `JString` — JSON type for strings
- `JNumber` — JSON type for numbers
- `JBoolean` — JSON type for booleans
- `JNull` — JSON type for null values
- `JArray` — JSON type for arrays
- `JObject` — JSON type for objects

<h3>How to create</h3> 

```kotlin
val obj = JObject(listOf(
  JField("name", JString("João")),
  JField("age", JNumber(30)),
  JField("active", JBoolean(true))
))
```

---

<h3>Convert to JSON text</h3> 

Use the `toText()` method to convert to JSON formated text with the right identation:

```kotlin
println(obj.toText())
```

Returns:

```json
{
  "name": "João",
  "age": 30,
  "active": true
}
```

<h3>Converting Kotlin objects to JSON</h3> 

Use `instantiateJsonModel()` to convert data classes, lists, maps, and primitive values to JSON structure:

```kotlin
data class User(val name: String, val age: Int)

val user = User("Ana", 25)
val json = instantiateJsonModel(user)

println(json.toText())
```
Returns

```json
{
  "name": "Ana",
  "age": 25
}
```
<h3>Other methods</h3>

 - `validateKeys()` validates that there are no duplciate or empty keys in a JObject
 - `validateArrayTypes()` validates that a JArray have values of the same type (ignoring nulls)
 - `filter` filters through JArrays and JObjects according to a predicate, creating a new JArray/JObject with only the filetered fields
 - `map` transforms the JArray elements according to a function passed as a parameter 


</br>
<h1>GetJson Framework</h1>

GetJson is a small Kotlin framework that handles HTTP GET endpoints returning JSON responses, it integrates seamlessly with the Json Model Library to automatically convert Kotlin return values into JSON.

The framework works by enumerating a list of REST controllers. These controllers define endpoints through annotations, enabling a declarative way to map URL segments and query parameters to function arguments.

<h2>How to use</h2>

First create a controller, and use the annotations to define the endpoints.

```kotlin
@Mapping("api")
class Controller {

    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

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
```
<h3>How to start the server</h3>

```kotlin
fun main() {
    val app = GetJson(Controller::class)
    app.startServer(8080)
}
```
<h3>Example of GET request urls</h3>

 - Using the url `http://localhost:8080/api/ints` you get `[1,2,3]` in its JSON representation<br>
 - Using the url `http://localhost:8080/api/path/JSON-Library` you get `"JSON-Library!"` in its JSON representation<br>
 - Using the url `http://localhost:8080/api/args?n=3&text=JSON` you get `{"JSON":"JSONJSONJSON"}` in its JSON representation<br>
<br>

<h2>UML Class Diagram</h2>
 
![Diagrama de Classes UML](https://github.com/user-attachments/assets/b38ffdde-8c7d-446c-a31a-4ac3c944b41e)
