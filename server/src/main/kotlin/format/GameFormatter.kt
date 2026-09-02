package alfredabdo.ktor.idlegame.format

import kotlin.time.Duration

object GameFormatter {

    fun formatDuration(duration: Duration): String = duration.toComponents { hours, minutes, seconds, nanoseconds ->
        buildString {
            val hasHours = hours != 0L
            val hasMinutes = minutes != 0
            val hasSeconds = seconds != 0
            val hasMilliseconds = nanoseconds != 0

            var components = 0
            if (hasHours) {
                append(hours).append('h')
                components++
            }
            if (
                hasMinutes ||
                ((hasSeconds || hasMilliseconds) && hasHours)
            ) {
                if (components++ > 0) append(' ')
                append(minutes).append('m')
            }
            if (
                hasSeconds ||
                (hasMilliseconds && (hasMinutes || hasHours))
            ) {
                if (components++ > 0) append(' ')
                append(seconds).append('s')
            }
            if (hasMilliseconds) {
                if (components > 0) append(' ')
                append((nanoseconds / 1_000_000).toString().padStart(3, '0')).append("ms")
            }
        }
    }
}