package alfredabdo.ktor.idlegame.database.types

import org.jetbrains.exposed.v1.core.ColumnTransformer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object DurationColumnTransformer : ColumnTransformer<Long, Duration> {
    override fun unwrap(value: Duration): Long = value.inWholeMilliseconds
    override fun wrap(value: Long): Duration = value.milliseconds
}