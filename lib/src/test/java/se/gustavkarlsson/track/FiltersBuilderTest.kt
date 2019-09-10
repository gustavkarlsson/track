package se.gustavkarlsson.track

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.Test

class FiltersBuilderTest {

    @Test
    fun `correct filters added`() {
        val filters = FiltersBuilder().apply {
            timestamp isLessThan 1
            id isGreaterThan 2
            appVersion isEqualTo 3
            value isNotEqualTo "foo"
        }.build()

        assertThat(filters).containsExactly(
            Filter(Field.Timestamp, Operator.LessThan, 1),
            Filter(Field.Id, Operator.GreaterThan, 2),
            Filter(Field.AppVersion, Operator.Equals, 3),
            Filter(Field.Value, Operator.NotEquals, "foo")
        )
    }
}
