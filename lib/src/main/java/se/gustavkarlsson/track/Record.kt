package se.gustavkarlsson.track

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * A record of an event stored in [Track].
 *
 * Records are created by assigning a [value] to a [key].
 * Each record gets a unique [id] and some additional metadata in [appVersion] and [timestamp].
 */
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
) : Parcelable, Serializable {

    internal constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeLong(id)
            writeString(key)
            writeLong(timestamp)
            writeLong(appVersion)
            writeString(value)
        }
    }

    override fun describeContents() = 0

    internal companion object CREATOR : Parcelable.Creator<Record> {
        override fun createFromParcel(parcel: Parcel) = Record(parcel)

        override fun newArray(size: Int): Array<Record?> = arrayOfNulls(size)
    }
}
