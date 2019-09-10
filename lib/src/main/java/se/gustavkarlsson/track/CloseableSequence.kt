package se.gustavkarlsson.track

import java.io.Closeable

interface CloseableSequence<T> : Closeable, Sequence<T> {
	val isClosed: Boolean
}
