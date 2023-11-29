package se.gustavkarlsson.track.sqlite

import se.gustavkarlsson.track.Order
import se.gustavkarlsson.track.Order.InsertionAscending
import se.gustavkarlsson.track.Order.InsertionDescending
import se.gustavkarlsson.track.Order.TimestampAscending
import se.gustavkarlsson.track.Order.TimestampDescending

internal fun Order.toOrderBySql(): String = when (this) {
    InsertionAscending -> "${Table.COLUMN_ID} ASC"
    InsertionDescending -> "${Table.COLUMN_ID} DESC"
    TimestampAscending -> "${Table.COLUMN_TIMESTAMP} ASC, ${Table.COLUMN_ID} ASC"
    TimestampDescending -> "${Table.COLUMN_TIMESTAMP} DESC, ${Table.COLUMN_ID} DESC"
}
