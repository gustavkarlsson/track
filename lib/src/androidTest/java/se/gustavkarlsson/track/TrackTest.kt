package se.gustavkarlsson.track

import android.content.Context
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
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
import java.nio.file.Files
import java.nio.file.Path

@MediumTest
class TrackTest {
    private val key = "key"
    private val value = "value"
    private val otherValue = "new_value"

    @Test
    fun emptyDatabaseFileName() = test(autoInitialize = false) {
        expectThrows<IllegalArgumentException> {
            initialize("")
        }
    }

    @Test
    fun blankDatabaseFileName() = test(autoInitialize = false) {
        expectThrows<IllegalArgumentException> {
            initialize(" \n\t")
        }
    }

    @Test
    fun initializeTwice() = test(autoInitialize = false) {
        expectThrows<IllegalStateException> {
            initialize("a.db")
            initialize("b.db")
        }
    }

    @Test
    fun accessUninitialized() = test(autoInitialize = false) {
        expectThrows<IllegalStateException> {
            get(key)
        }
    }

    @Test
    fun setNonexistentKey() = test {
        val replaced = set(key, value)
        val record = get(key)

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
        test {
            set(key, value)
            val replaced = set(key, otherValue)
            val record = get(key)

            expect {
                that(replaced).describedAs("replaced").isTrue()
                that(record).describedAs("record")
                    .isNotNull()
                    .get(Record::value)
                    .isEqualTo(otherValue)
            }
        }
    }

    @Test
    fun setDoesNotOverwriteAdd() = test {
        add(key, value)
        val replaced = set(key, otherValue)
        val count = query(key).count()

        expect {
            that(replaced).describedAs("replaced").isFalse()
            that(count).describedAs("count").isEqualTo(2)
        }
    }

    @Test
    fun getDoesNotReadFromAdd() = test {
        add(key, value)
        val record = get(key)

        expectThat(record).describedAs("record").isNull()
    }

    @Test
    fun queryGetsAllValues() = test {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { add(key, it) }
        set(key, "foobar")

        val records = query(key)

        expectThat(records).describedAs("records")
            .map(Record::value)
            .containsExactlyInAnyOrder(values + "foobar")
    }

    @Test
    fun removeSetValueById() = test {
        set(key, "foobar")

        val record = get(key)
        val removed = remove(record!!.id)
        val queriedRecords = query(key)

        expect {
            that(removed).describedAs("removed").isTrue()
            that(queriedRecords).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByKey() = test {
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { add(key, it) }
        set(key, "foobar")

        val removed = remove(key)
        val queried = query(key)

        expect {
            that(removed).describedAs("removed").isEqualTo(values.count() + 1)
            that(queried).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByFilter() = test {
        ('a'..'z').forEach { add(key, it.toString()) }

        val removed = remove {
            it.id <= 10
        }
        val queried = query(key)

        expect {
            that(removed).describedAs("removed count").isEqualTo(10)
            that(queried).describedAs("queried records").hasSize(16)
            that(queried).describedAs("queried record ID:s").map(Record::id).all { isGreaterThan(10L) }
        }
    }

    @Test
    fun noReusedIds() = test {
        add(key)
        add(key)
        add(key)
        remove(key)
        add(key)

        val lastId = query(key).last().id

        expectThat(lastId).describedAs("last ID").isGreaterThan(2L)
    }

    @Test
    fun idsAlwaysIncrementing() = test {
        add(key, "1")
        add(key, "2")
        add(key, "3")
        remove(2)
        add(key, "4")
        remove(1)
        add(key, "5")
        add(key, "6")

        val allIdsIncrementing = query(key)
            .zipWithNext { prev, curr -> curr.id > prev.id }
            .all { it }

        expectThat(allIdsIncrementing).describedAs("ID's increment").isTrue()
    }

    @Test
    fun settingValueCreatesDatabase() = test {
        set(key)
        expectThat(databases.first()).exists()
    }

    @Test
    fun clearDeletesDatabase() = test {
        set(key)
        clear()
        expectThat(databases.first()).doesNotExist()
    }

    @Test
    fun clearAllowsReuse() = test {
        set(key)
        clear()
        set(key)
        val record = get(key)
        expectThat(record).describedAs("record").isNotNull()
    }
}

private fun test(autoInitialize: Boolean = true, block: TestTrack.() -> Unit) {
    TestTrack().use { testTrack ->
        if (autoInitialize) testTrack.initialize("track_test.db")
        testTrack.block()
    }
}

private class TestTrack : Track by Track, AutoCloseable {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    private val databaseNames = mutableSetOf<String>()

    val databases
        get() = databaseNames.mapNotNull {
            try {
                context.getDatabasePath(it).toPath()
            } catch (e: Exception) {
                null
            }
        }

    fun initialize(databaseFileName: String) {
        databaseNames.add(databaseFileName)
        Track.initialize(context, databaseFileName)
    }

    fun reset() {
        Track.initializedDelegate = null
        databases.forEach { Files.deleteIfExists(it) }
    }

    override fun close() = reset()
}

private fun <T : Path> Assertion.Builder<T>.doesNotExist(): Assertion.Builder<T> =
    assertThat("does not exist") { !Files.exists(it) }
