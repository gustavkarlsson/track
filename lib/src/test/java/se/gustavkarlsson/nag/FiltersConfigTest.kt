package se.gustavkarlsson.nag

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.Test

class FiltersConfigTest {

	@Test
	fun `correct filters added`() {
		val filters = FiltersConfig().apply {
			before(1)
			after(2)
			versionIs(3)
			versionIsNot(4)
			versionLessThan(5)
			versionGreaterThan(6)
			valueIs("7")
			valueIsNot("8")
		}.filters

		assertThat(filters).containsExactly(
			Filter.Before(1),
			Filter.After(2),
			Filter.VersionIs(3),
			Filter.VersionIsNot(4),
			Filter.VersionLessThan(5),
			Filter.VersionGreaterThan(6),
			Filter.ValueIs("7"),
			Filter.ValueIsNot("8")
		)
	}
}
