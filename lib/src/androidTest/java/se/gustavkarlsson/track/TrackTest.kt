@file:OptIn(ExperimentalCoroutinesApi::class)

package se.gustavkarlsson.track

import android.content.Context
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import strikt.api.Assertion
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.containsExactlyInAnyOrder
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
    private val key = "key"
    private val value = "value"
    private val otherValue = "new_value"

    @Test
    fun emptyDatabaseFileName() = testSingleton(autoInitialize = false) { track ->
        expectThrows<IllegalArgumentException> {
            track.initialize("")
        }
    }

    @Test
    fun blankDatabaseFileName() = testSingleton(autoInitialize = false) { track ->
        expectThrows<IllegalArgumentException> {
            track.initialize(" \n\t")
        }
    }

    @Test
    fun initializeTwice() = testSingleton(autoInitialize = false) { track ->
        expectThrows<IllegalStateException> {
            track.initialize("a.db")
            track.initialize("b.db")
        }
    }

    @Test
    fun accessUninitialized() = testSingleton(autoInitialize = false) { track ->
        expectThrows<IllegalStateException> {
            track.get(key)
        }
    }

    @Test
    fun setNonexistentKey() = testSingleton { track ->
        val replaced = track.set(key, value)
        val record = track.get(key)

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
        testSingleton { track ->
            track.set(key, value)
            val replaced = track.set(key, otherValue)
            val record = track.get(key)

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
    fun setDoesNotOverwriteAdd() = testSingleton { track ->
        track.add(key, value)
        val replaced = track.set(key, otherValue)
        val count = track.query(key).count()

        expect {
            that(replaced).describedAs("replaced").isFalse()
            that(count).describedAs("count").isEqualTo(2)
        }
    }

    @Test
    fun getDoesNotReadFromAdd() = testSingleton { track ->
        track.add(key, value)
        val record = track.get(key)

        expectThat(record).describedAs("record").isNull()
    }

    @Test
    fun queryGetsAllValues() = testSingleton { track ->
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { track.add(key, it) }
        track.set(key, "foobar")

        val records = track.query(key)

        expectThat(records).describedAs("records")
            .map(Record::value)
            .containsExactlyInAnyOrder(values + "foobar")
    }

    @Test
    fun removeSetValueById() = testSingleton { track ->
        track.set(key, "foobar")

        val record = track.get(key)
        val removed = track.remove(record!!.id)
        val queriedRecords = track.query(key)

        expect {
            that(removed).describedAs("removed").isTrue()
            that(queriedRecords).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByKey() = testSingleton { track ->
        val values = ('a'..'z').map(Char::toString).toSet()
        values.forEach { track.add(key, it) }
        track.set(key, "foobar")

        val removed = track.remove(key)
        val queried = track.query(key)

        expect {
            that(removed).describedAs("removed").isEqualTo(values.count() + 1)
            that(queried).describedAs("queried records").isEmpty()
        }
    }

    @Test
    fun removeByFilter() = testSingleton { track ->
        ('a'..'z').forEach { track.add(key, it.toString()) }

        val removed = track.remove {
            it.id <= 10
        }
        val queried = track.query(key)

        expect {
            that(removed).describedAs("removed count").isEqualTo(10)
            that(queried).describedAs("queried records").hasSize(16)
            that(queried).describedAs("queried record ID:s").map(Record::id).all { isGreaterThan(10L) }
        }
    }

    @Test
    fun noReusedIds() = testSingleton { track ->
        track.add(key)
        track.add(key)
        track.add(key)
        track.remove(key)
        track.add(key)

        val lastId = track.query(key).last().id

        expectThat(lastId).describedAs("last ID").isGreaterThan(2L)
    }

    @Test
    fun idsAlwaysIncrementing() = testSingleton { track ->
        track.add(key, "1")
        track.add(key, "2")
        track.add(key, "3")
        track.remove(2)
        track.add(key, "4")
        track.remove(1)
        track.add(key, "5")
        track.add(key, "6")

        val allIdsIncrementing = track.query(key)
            .zipWithNext { prev, curr -> curr.id > prev.id }
            .all { it }

        expectThat(allIdsIncrementing).describedAs("ID's increment").isTrue()
    }

    @Test
    fun settingValueCreatesDatabase() = testSingleton { track ->
        track.set(key)
        expectThat(track.databases.first()).exists()
    }

    @Test
    fun clearDeletesDatabase() = testSingleton { track ->
        track.set(key)
        track.clear()
        expectThat(track.databases.first()).doesNotExist()
    }

    @Test
    fun clearAllowsReuse() = testSingleton { track ->
        track.set(key)
        track.clear()
        track.set(key)
        val record = track.get(key)
        expectThat(record).describedAs("record").isNotNull()
    }

    @Test
    fun createWithEmptyDatabaseName() {
        expectThrows<IllegalArgumentException> {
            testCreated("")
        }
    }

    @Test
    fun createWithBlankDatabaseName() {
        expectThrows<IllegalArgumentException> {
            testCreated(" \n\t")
        }
    }

    @Test
    fun differentCreatedDatabasesHoldsDifferent() {
        testCreated("a.db") { a ->
            testCreated("b.db") { b ->
                a.set(key, value)
                b.set(key, otherValue)

                val recordA = a.get(key)
                val recordB = b.get(key)

                expect {
                    that(recordA).describedAs("record from database a")
                        .isNotNull()
                        .get("value from database a", Record::value)
                        .isEqualTo(value)
                    that(recordB).describedAs("record from database b")
                        .isNotNull()
                        .get("value from database b", Record::value)
                        .isEqualTo(otherValue)
                }
            }
        }
    }
}

private fun testSingleton(autoInitialize: Boolean = true, block: suspend (track: TestSingletonTrack) -> Unit = {}) {
    runTest {
        TestSingletonTrack().use { testTrack ->
            if (autoInitialize) testTrack.initialize("track_test.db")
            block(testTrack)
        }
    }
}

private fun testCreated(databaseFileName: String, block: suspend (track: TestCreatedTrack) -> Unit = {}) {
    runTest {
        TestCreatedTrack(databaseFileName).use { testTrack ->
            testTrack.initialize()
            block(testTrack)
        }
    }
}

private class TestSingletonTrack : Track by Track, AutoCloseable {

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

    override fun close() {
        Track.initializedDelegate = null
        databases.forEach { Files.deleteIfExists(it) }
    }
}

private class TestCreatedTrack(private val databaseFileName: String) : Track, AutoCloseable {
    private lateinit var delegate: Track

    val database: Path?
        get() = context.getDatabasePath(databaseFileName).toPath()

    fun initialize() {
        delegate = Track.create(context, databaseFileName)
    }

    override fun close() {
        database?.let(Files::deleteIfExists)
    }

    override suspend fun get(key: String) = delegate.get(key)

    override suspend fun set(key: String, value: String) = delegate.set(key, value)

    override suspend fun <T> query(key: String, selector: (Sequence<Record>) -> T) = delegate.query(key, selector)

    override suspend fun add(key: String, value: String) = delegate.add(key, value)

    override suspend fun remove(id: Long) = delegate.remove(id)

    override suspend fun remove(key: String) = delegate.remove(key)

    override suspend fun remove(filter: (Record) -> Boolean) = delegate.remove(filter)

    override suspend fun clear() = delegate.clear()
}

private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().context

private fun <T : Path> Assertion.Builder<T>.exists(): Assertion.Builder<T> =
    assertThat("exists") { Files.exists(it) }

private fun <T : Path> Assertion.Builder<T>.doesNotExist(): Assertion.Builder<T> =
    assertThat("does not exist") { !Files.exists(it) }
