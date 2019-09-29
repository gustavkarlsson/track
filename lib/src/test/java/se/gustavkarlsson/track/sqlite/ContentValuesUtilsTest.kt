package se.gustavkarlsson.track.sqlite

import android.content.ContentValues
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Test

class ContentValuesUtilsTest {

    private val mockContentValues = mock<ContentValues>()

    @Before
    fun setUp() {
        constructor = { mockContentValues }
    }

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

        map.toContentValues()

        verify(mockContentValues).put("Boolean", true)
        verify(mockContentValues).put("ByteArray", byteArrayOf(1))
        verify(mockContentValues).put("Byte", 2.toByte())
        verify(mockContentValues).put("Short", 3.toShort())
        verify(mockContentValues).put("Int", 4)
        verify(mockContentValues).put("Long", 5.toLong())
        verify(mockContentValues).put("Float",6.toFloat())
        verify(mockContentValues).put("Double", 7.0)
        verify(mockContentValues).put("String", "Foo")
        verify(mockContentValues).putNull("null")
        verifyNoMoreInteractions(mockContentValues)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toContentValues with unsupported type`() {
        val map = mapOf("foo" to emptyList<String>())

        map.toContentValues()
    }
}
