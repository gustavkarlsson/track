package se.gustavkarlsson.track.sqlite

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import strikt.api.expectThrows

class ContentValuesUtilsTest {

    @Test
    fun `toContentValues with all supported data types`() {
        val map = mapOf(
            "Boolean" to true,
            "ByteArray" to byteArrayOf(1),
            "Byte" to 2.toByte(),
            "Short" to 3.toShort(),
            "Int" to 4,
            "Long" to 5.toLong(),
            "Float" to 6.toFloat(),
            "Double" to 7.0,
            "String" to "Foo",
            "null" to null
        )

        val contentValues = map.toContentValues { mock() }

        verify(contentValues).put("Boolean", true)
        verify(contentValues).put("ByteArray", byteArrayOf(1))
        verify(contentValues).put("Byte", 2.toByte())
        verify(contentValues).put("Short", 3.toShort())
        verify(contentValues).put("Int", 4)
        verify(contentValues).put("Long", 5.toLong())
        verify(contentValues).put("Float", 6.toFloat())
        verify(contentValues).put("Double", 7.0)
        verify(contentValues).put("String", "Foo")
        verify(contentValues).putNull("null")
        verifyNoMoreInteractions(contentValues)
    }

    @Test
    fun `toContentValues with unsupported type`() {
        val map = mapOf("foo" to emptyList<String>())

        expectThrows<IllegalArgumentException> { map.toContentValues { mock() } }
    }
}
