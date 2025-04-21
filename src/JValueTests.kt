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
        assertEquals("", JNull().toText())
    }

}