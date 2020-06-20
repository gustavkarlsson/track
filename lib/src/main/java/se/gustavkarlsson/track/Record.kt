package se.gustavkarlsson.track

import android.os.Parcelable
import java.io.Serializable
import kotlinx.android.parcel.Parcelize

/**
 * A record of an event stored in [Track].
 *
 * Records are created by assigning a [value] to a [key].
 * Each record gets a unique [id] and some additional metadata in [appVersion] and [timestamp].
 */
@Parcelize
data class Record(
    /**
     * A unique id.
     */
    val id: Long,
    /**
     * The key of this record.
     */
    val key: String,
    /**
     * A timestamp (milliseconds from epoch) of when this record was created.
     */
    val timestamp: Long,
    /**
     * The version code of the app when this record was created.
     */
    val appVersion: Long,
    /**
     * The value assigned to this record.
     */
    val value: String
) : Parcelable, Serializable
