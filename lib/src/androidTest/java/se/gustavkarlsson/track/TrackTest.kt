package se.gustavkarlsson.track

import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.gustavkarlsson.track.sqlite.Database

@MediumTest
class TrackTest {

    private val key = "key"
    private val value = "value"
    private val otherValue = "new_value"

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val database = context.getDatabasePath(Database.NAME)
        if (database.exists() && !database.delete()) {
            fail("Could not delete existing database $database")
        }
        Track.initialize(context)
    }

    @After
    fun tearDown() {
        Track.deleteDatabase()
    }

    @Test
    fun setNonexistentKey() {
        val replaced = Track.set(key, value)
        val record = Track.get(key)

        assertAll {
            assertThat(replaced).isFalse()
            assertThat(record).isNotNull()
            assertThat(record!!.value).isEqualTo(value)
        }
    }

    @Test
    fun setExistingKey() {
        Track.set(key, value)
        val replaced = Track.set(key, otherValue)
        val record = Track.get(key)

        assertAll {
            assertThat(replaced).isTrue()
            assertThat(record!!.value).isEqualTo(otherValue)
        }
    }

    @Test
    fun setDoesNotOverwriteAdd() {
        Track.add(key, value)
        val replaced = Track.set(key, otherValue)
        val count = Track.query(key).count()

        assertAll {
            assertThat(replaced).isFalse()
            assertThat(count).isEqualTo(2)
        }
    }

    @Test
    fun getDoesNotReadFromAdd() {
        Track.add(key, value)
        val record = Track.get(key)

        assertThat(record).isNull()
    }

    @Test
    fun queryGetsAllValues() {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { Track.add(key, it) }
        Track.set(key, "foobar")

        val records = Track.query(key)

        val actualValues = records.map(Record::value).toSet()
        assertThat(actualValues).isEqualTo(values + "foobar")
    }

    @Test
    fun removeSetValueById() {
        Track.set(key, "foobar")

        val record = Track.get(key)
        val removed = Track.remove(record!!.id)
        val queried = Track.query(key)

        assertAll {
            assertThat(removed).isTrue()
            assertThat(queried).isEmpty()
        }
    }

    @Test
    fun removeByKey() {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { Track.add(key, it) }
        Track.set(key, "foobar")

        val removed = Track.remove(key)
        val queried = Track.query(key)

        assertAll {
            assertThat(removed).isEqualTo(values.count() + 1)
            assertThat(queried).isEmpty()
        }
    }

    @Test
    fun removeByFilter() {
        ('a'..'z').forEach { Track.add(key, it.toString()) }

        val removed = Track.remove {
            it.id <= 10
        }
        val queried = Track.query(key)

        assertAll {
            assertThat(removed).isEqualTo(10)
            assertThat(queried).hasSize(16)
            assertThat(queried.map(Record::id)).each { it.isGreaterThan(10) }
        }
    }
}
