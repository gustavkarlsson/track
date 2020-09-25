package se.gustavkarlsson.track.sqlite

import org.junit.Test
import strikt.api.expect
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class SelectionTest {

    @Test
    fun empty_selections() {
        emptyList<Selection>().assert(null)
    }

    @Test
    fun equals_string() {
        val selections = listOf(
            Selection("column", Operator.Equals, "foo")
        )

        selections.assert("column = ?", "foo")
    }

    @Test
    fun greater_than_long() {
        val selections = listOf(
            Selection("column", Operator.GreaterThan, 2L)
        )

        selections.assert("column > ?", "2")
    }

    @Test
    fun lesser_than_float() {
        val selections = listOf(
            Selection("column", Operator.LessThan, 2.toFloat())
        )

        selections.assert("column < ?", "2.0")
    }

    @Test
    fun not_equals_boolean() {
        val selections = listOf(
            Selection("column", Operator.NotEquals, true)
        )

        selections.assert("column <> ?", "1")
    }

    @Test
    fun in_empty() {
        val selections = listOf(
            Selection("column", Operator.In, emptyList<Int>())
        )

        selections.assert("column IN ()")
    }

    @Test
    fun in_ints() {
        val selections = listOf(
            Selection("column", Operator.In, listOf(1, 2, 3))
        )

        selections.assert("column IN (1, 2, 3)")
    }

    @Test
    fun multiple_values() {
        val selections = listOf(
            Selection("column1", Operator.GreaterThan, 2),
            Selection("column2", Operator.Equals, "foo")
        )

        selections.assert("column1 > ? AND column2 = ?", "2", "foo")
    }

    @Test
    fun mixed_equals_and_in() {
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

    expect {
        that(sql).describedAs("sql").isEqualTo(expectedSql)
        that(args.toList()).describedAs("args").containsExactly(*expectedArgs)
    }
}
