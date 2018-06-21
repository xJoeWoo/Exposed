package org.jetbrains.exposed.extensions.dataTypes.javaTime

import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*


/**
 * A date column to store a date.
 *
 * @param name The column name
 */
fun Table.date(name: String): DateColumn<LocalDate> = registerColumn(DateColumn(this, name, Java8ColumnType(DateType.DATE)))

/**
 * A datetime column to store both a date and a time.
 *
 * @param name The column name
 */
fun Table.datetime(name: String): DateColumn<LocalDateTime> = registerColumn(DateColumn(this, name, Java8ColumnType(DateType.DATETIME)))

@JvmName("ExposedJava8AsDate")
fun Expression<LocalDateTime>.asDate() = NoOpConversion<LocalDateTime, LocalDate>(this, Java8ColumnType(DateType.DATE))
@JvmName("ExposedJava8AsDateNullable")
fun Expression<LocalDateTime?>.asDate() = NoOpConversion<LocalDateTime?, LocalDate?>(this, Java8ColumnType(DateType.DATE))

private val DEFAULT_DATE_STRING_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
private val DEFAULT_DATE_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.ROOT)
private val SQLITE_DATE_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

class Java8ColumnType(type: DateType): DateColumnType(type) {

    private fun Long.millisToLocalDateTimeUTC() = LocalDateTime.ofEpochSecond(this / 1000, (this % 1000).toInt(), ZoneOffset.UTC)

    override fun nonNullValueToString(value: Any): String {
        if (value is String) return value

        val dateTime = when (value) {
            is LocalDate -> value.atStartOfDay()
            is LocalDateTime -> value
            is java.sql.Date -> value.time.millisToLocalDateTimeUTC()
            is java.sql.Timestamp -> value.time.millisToLocalDateTimeUTC()
            else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
        }

        return if (type == DateType.DATETIME)
            "'${DEFAULT_DATE_TIME_STRING_FORMATTER.format(dateTime.atZone(ZoneId.systemDefault()))}'"
        else
            "'${DEFAULT_DATE_STRING_FORMATTER.format(dateTime)}'"
    }

    override fun valueFromDB(value: Any): Any = when(value) {
        is LocalDateTime -> value
        is TemporalAccessor -> LocalDateTime.from(value)
        is java.sql.Date ->  value.time.millisToLocalDateTimeUTC()
        is java.sql.Timestamp -> value.time.millisToLocalDateTimeUTC()
        is Int -> value.toLong().millisToLocalDateTimeUTC()
        is Long -> value.millisToLocalDateTimeUTC()
        else -> when {
            isSQLite -> LocalDateTime.parse("$value", SQLITE_DATE_TIME_STRING_FORMATTER)
            else -> LocalDateTime.parse("$value", DEFAULT_DATE_TIME_STRING_FORMATTER)
        }
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is LocalDateTime) {
            val millis = value.toInstant(ZoneOffset.UTC).toEpochMilli()
            return if (type == DateType.DATETIME) {
                java.sql.Timestamp(millis)
            } else {
                java.sql.Date(millis)
            }
        }
        return value
    }
}