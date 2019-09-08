package se.gustavkarlsson.nag

import java.io.Closeable

interface CloseableSequence<T> : Closeable, Sequence<T> {
	val isClosed: Boolean
}
