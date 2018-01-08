package org.jetbrains.exposed.extensions.dataTypes.joda

import org.jetbrains.exposed.sql.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import java.util.*


fun Table.date(name: String): Column<DateTime> = registerColumn(name, JodaDateColumnType(DateType.DATE))
fun Table.datetime(name: String): Column<DateTime> = registerColumn(name, JodaDateColumnType(DateType.DATETIME))


private val DEFAULT_DATE_STRING_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd").withLocale(Locale.ROOT)
private val DEFAULT_DATE_TIME_STRING_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSSSSS").withLocale(Locale.ROOT)
private val SQLITE_DATE_TIME_STRING_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
private val SQLITE_DATE_STRING_FORMATTER = ISODateTimeFormat.yearMonthDay()

class JodaDateColumnType(type: DateType): DateColumnType(type) {

    override fun nonNullValueToString(value: Any): String {
        if (value is String) return value

        val dateTime = when (value) {
            is DateTime -> value
            is java.sql.Date -> DateTime(value.time)
            is java.sql.Timestamp -> DateTime(value.time)
            else -> error("Unexpected value: $value")
        }

        return if (type == DateType.DATETIME)
            "'${DEFAULT_DATE_TIME_STRING_FORMATTER.print(dateTime.toDateTime(DateTimeZone.getDefault()))}'"
        else
            "'${DEFAULT_DATE_STRING_FORMATTER.print(dateTime)}'"
    }

    override fun valueFromDB(value: Any): Any = when(value) {
        is DateTime -> value
        is java.sql.Date ->  DateTime(value.time)
        is java.sql.Timestamp -> DateTime(value.time)
        is Int -> DateTime(value.toLong())
        is Long -> DateTime(value)
        is String -> when {
            isSQLite && type == DateType.DATETIME -> SQLITE_DATE_TIME_STRING_FORMATTER.parseDateTime(value)
            isSQLite -> SQLITE_DATE_STRING_FORMATTER.parseDateTime(value)
            else -> value
        }
        else -> DEFAULT_DATE_TIME_STRING_FORMATTER.parseDateTime(value.toString())
    }

    override fun notNullValueToDB(value: Any): Any {
        if (value is DateTime) {
            val millis = value.millis
            return if (type == DateType.DATETIME) {
                java.sql.Timestamp(millis)
            } else {
                java.sql.Date(millis)
            }
        }
        return value
    }
}