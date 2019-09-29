package se.gustavkarlsson.track.sqlite

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.Test

class SelectionTest {

    @Test
    fun `empty selections`() {
        emptyList<Selection>().assert(null)
    }

    @Test
    fun `equals string`() {
        val selections = listOf(
            Selection("column", Operator.Equals, "foo")
        )

        selections.assert("column = ?", "foo")
    }

    @Test
    fun `greater than long`() {
        val selections = listOf(
            Selection("column", Operator.GreaterThan, 2L)
        )

        selections.assert("column > ?", "2")
    }

    @Test
    fun `lesser than float`() {
        val selections = listOf(
            Selection("column", Operator.LessThan, 2.toFloat())
        )

        selections.assert("column < ?", "2.0")
    }

    @Test
    fun `not equals boolean`() {
        val selections = listOf(
            Selection("column", Operator.NotEquals, true)
        )

        selections.assert("column <> ?", "1")
    }

    @Test
    fun `in empty`() {
        val selections = listOf(
            Selection("column", Operator.In, emptyList<Int>())
        )

        selections.assert("column IN ()")
    }

    @Test
    fun `in ints`() {
        val selections = listOf(
            Selection("column", Operator.In, listOf(1, 2, 3))
        )

        selections.assert("column IN (1, 2, 3)")
    }

    @Test
    fun `multiple values`() {
        val selections = listOf(
            Selection("column1", Operator.GreaterThan, 2),
            Selection("column2", Operator.Equals, "foo")
        )

        selections.assert("column1 > ? AND column2 = ?", "2", "foo")
    }

    @Test
    fun `mixed equals and in`() {
        val selections = listOf(
            Selection("column1", Operator.Equals, 2),
            Selection("column2", Operator.In, listOf(2.0, 3.0))
        )

        selections.assert("column1 = ? AND column2 IN (2.0, 3.0)", "2")
    }
}

private fun List<Selection>.assert(expectedSql: String?, vararg expectedArgs: String) {
    val sql = toSelectionSql()
    val args = toSelectionArgSql()

    assertAll {
        assertThat(sql, "expectedSql").isEqualTo(expectedSql)
        assertThat(args, "expectedArgs").containsExactly(*expectedArgs)
    }
}
