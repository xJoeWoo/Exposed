package org.jetbrains.exposed.extensions.dataTypes.javaTime

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.DateType
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * A date column to store a date.
 *
 * @param name The column name
 */
fun Table.date(name: String): Column<LocalDateTime> = registerColumn(name, JavaLocalDateColumnType(DateType.DATE))

/**
 * A datetime column to store both a date and a time.
 *
 * @param name The column name
 */
fun Table.datetime(name: String): Column<LocalDateTime> = registerColumn(name, JavaLocalDateColumnType(DateType.DATETIME))

private val DEFAULT_DATE_STRING_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.ROOT)
private val DEFAULT_DATE_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSSSSS", Locale.ROOT)
private val SQLITE_DATE_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
private val SQLITE_DATE_STRING_FORMATTER = DateTimeFormatter.ISO_DATE

class JavaLocalDateColumnType(type: DateType): DateColumnType(type) {

    private fun Long.millisToLocalDateTimeUTC() = LocalDateTime.ofEpochSecond(this / 1000, (this % 1000).toInt(), ZoneOffset.UTC)

    override fun nonNullValueToString(value: Any): String {
        if (value is String) return value

        val dateTime = when (value) {
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
        is java.sql.Date ->  value.time.millisToLocalDateTimeUTC()
        is java.sql.Timestamp -> value.time.millisToLocalDateTimeUTC()
        is Int -> value.toLong().millisToLocalDateTimeUTC()
        is Long -> value.millisToLocalDateTimeUTC()
        is String -> when {
            isSQLite && type == DateType.DATETIME -> SQLITE_DATE_TIME_STRING_FORMATTER.parse(value)
            isSQLite -> SQLITE_DATE_STRING_FORMATTER.parse(value)
            else -> value
        }
        else -> DEFAULT_DATE_TIME_STRING_FORMATTER.parse(value.toString())
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