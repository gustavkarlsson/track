package se.gustavkarlsson.track

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Record(
    val id: Long,
    val key: String,
    val timestamp: Long,
    val appVersion: Long,
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
