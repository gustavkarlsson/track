package se.gustavkarlsson.track

import android.content.Context
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import java.nio.file.Files
import java.nio.file.Path
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.Assertion
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.exists
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import strikt.assertions.map

@MediumTest
class TrackTest {
    private lateinit var context: Context
    private lateinit var databasePath: Path

    private val databaseName = "track_test.db"
    private val key = "key"
    private val value = "value"
    private val otherValue = "new_value"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
        databasePath = context.getDatabasePath(databaseName).toPath()
        Files.deleteIfExists(databasePath)
        Track.initialize(context, databaseName)
    }

    @After
    fun tearDown() {
        Files.deleteIfExists(databasePath)
        Track.initializedDelegate = null
    }

    @Test
    fun emptyDatabaseFileName() {
        Track.initializedDelegate = null
        expectThrows<IllegalArgumentException> {
            Track.initialize(context, "")
        }
    }

    @Test
    fun blankDatabaseFileName() {
        Track.initializedDelegate = null
        expectThrows<IllegalArgumentException> {
            Track.initialize(context, " \n\t")
        }
    }

    @Test
    fun initializeTwice() {
        expectThrows<IllegalStateException> {
            Track.initialize(context, databaseName)
        }
    }

    @Test
    fun accessUninitialized() {
        Track.initializedDelegate = null
        expectThrows<IllegalStateException> {
            Track.get(key)
        }
    }

    @Test
    fun setNonexistentKey() {
        val replaced = Track.set(key, value)
        val record = Track.get(key)

        expect {
            that(replaced).describedAs("replaced").isFalse()
            that(record).describedAs("record")
                .isNotNull()
                .get(Record::value)
                .isEqualTo(value)
        }
    }

    @Test
    fun setExistingKey() {
        Track.set(key, value)
        val replaced = Track.set(key, otherValue)
        val record = Track.get(key)

        expect {
            that(replaced).describedAs("replaced").isTrue()
            that(record).describedAs("record")
                .isNotNull()
                .get(Record::value)
                .isEqualTo(otherValue)
        }
    }

    @Test
    fun setDoesNotOverwriteAdd() {
        Track.add(key, value)
        val replaced = Track.set(key, otherValue)
        val count = Track.query(key).count()

        expect {
            that(replaced).describedAs("replaced").isFalse()
            that(count).describedAs("count").isEqualTo(2)
        }
    }

    @Test
    fun getDoesNotReadFromAdd() {
        Track.add(key, value)
        val record = Track.get(key)

        expectThat(record).describedAs("record").isNull()
    }

    @Test
    fun queryGetsAllValues() {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { Track.add(key, it) }
        Track.set(key, "foobar")

        val records = Track.query(key)

        expectThat(records).describedAs("records")
            .map(Record::value)
            .containsExactlyInAnyOrder(values + "foobar")
    }

    @Test
    fun removeSetValueById() {
        Track.set(key, "foobar")

        val record = Track.get(key)
        val removed = Track.remove(record!!.id)
        val queriedRecords = Track.query(key)

        expect {
            that(removed).describedAs("removed").isTrue()
            that(queriedRecords).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByKey() {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { Track.add(key, it) }
        Track.set(key, "foobar")

        val removed = Track.remove(key)
        val queried = Track.query(key)

        expect {
            that(removed).describedAs("removed").isEqualTo(values.count() + 1)
            that(queried).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByFilter() {
        ('a'..'z').forEach { Track.add(key, it.toString()) }

        val removed = Track.remove {
            it.id <= 10
        }
        val queried = Track.query(key)

        expect {
            that(removed).describedAs("removed count").isEqualTo(10)
            that(queried).describedAs("queried records").hasSize(16)
            that(queried).describedAs("queried record ID:s").map(Record::id).all { isGreaterThan(10L) }
        }
    }

    @Test
    fun noReusedIds() {
        Track.add(key)
        Track.add(key)
        Track.add(key)
        Track.remove(key)
        Track.add(key)

        val lastId = Track.query(key).last().id

        expectThat(lastId).describedAs("last ID").isGreaterThan(2L)
    }

    @Test
    fun idsAlwaysIncrementing() {
        Track.add(key, "1")
        Track.add(key, "2")
        Track.add(key, "3")
        Track.remove(2)
        Track.add(key, "4")
        Track.remove(1)
        Track.add(key, "5")
        Track.add(key, "6")

        val allIdsIncrementing = Track.query(key)
            .zipWithNext { prev, curr -> curr.id > prev.id }
            .all { it }

        expectThat(allIdsIncrementing).describedAs("ID's increment").isTrue()
    }

    @Test
    fun settingValueCreatesDatabase() {
        Track.set(key)
        expectThat(databasePath).exists()
    }

    @Test
    fun clearDeletesDatabase() {
        Track.set(key)
        Track.clear()
        expectThat(databasePath).doesNotExist()
    }

    @Test
    fun clearAllowsReuse() {
        Track.set(key)
        Track.clear()
        Track.set(key)
        val record = Track.get(key)
        expectThat(record).describedAs("record").isNotNull()
    }
}

private fun <T : Path> Assertion.Builder<T>.doesNotExist(): Assertion.Builder<T> =
    assertThat("does not exist") { !Files.exists(it) }
