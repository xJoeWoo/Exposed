package org.jetbrains.exposed.sql

import org.jetbrains.exposed.sql.vendors.SQLiteDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.text.SimpleDateFormat
import java.util.*

enum class DateType {
    INSTANT, DATETIME, DATE, TIME
}

abstract class DateColumnType(val type: DateType): ColumnType() {
    override fun sqlType(): String  = currentDialect.dataTypeProvider.dateTimeType(type)
    protected val isSQLite: Boolean get() = currentDialect is SQLiteDialect
}

abstract class DateApi<T:Any> {
    inner class Date(val expr: Expression<T?>) : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder): String
                = currentDialect.functionProvider.cast(expr, columnType, queryBuilder)
        override val columnType: IColumnType = columnType(DateType.DATE)
    }

    inner class CurrentDateTime : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder) = "CURRENT_TIMESTAMP"
        override val columnType: IColumnType = columnType(DateType.DATETIME)
    }

    inner class Month(val expr: Expression<T?>) : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder): String = "MONTH(${expr.toSQL(queryBuilder)})"
        override val columnType: IColumnType = columnType(DateType.DATE)
    }

    protected abstract fun columnType(type: DateType) : DateColumnType

    fun dateParam(value: T): Expression<T> = QueryParameter(value, columnType(DateType.DATE))
    fun dateTimeParam(value: T): Expression<T> = QueryParameter(value, columnType(DateType.DATETIME))

    fun dateLiteral(value: T) : LiteralOp<T> = LiteralOp(columnType(DateType.DATE), value)
    fun dateTimeLiteral(value: T) : LiteralOp<T> = LiteralOp(columnType(DateType.DATETIME), value)
}

object DefaultDateSPI : DateApi<java.util.Date>() {
    public override fun columnType(type: DateType): DateColumnType = DefaultDateColumnType(type)

    private val DEFAULT_DATE_STRING_FORMATTER get() = SimpleDateFormat("YYYY-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getDefault() }
    private val DEFAULT_DATE_TIME_STRING_FORMATTER get() = SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSSSSS", Locale.ROOT).apply { timeZone = TimeZone.getDefault() }
    private val SQLITE_DATE_TIME_STRING_FORMATTER get() = SimpleDateFormat("YYYY-MM-dd HH:mm:ss").apply { timeZone = TimeZone.getDefault() }
    private val SQLITE_DATE_STRING_FORMATTER get() = DEFAULT_DATE_STRING_FORMATTER



    private class DefaultDateColumnType(type: DateType) : DateColumnType(type) {
        override fun nonNullValueToString(value: Any): String {
            if (value is String) return value

            val dateTime = when (value) {
                is java.util.Date -> value
                is java.sql.Date -> java.util.Date(value.time)
                is java.sql.Timestamp -> java.util.Date(value.time)
                else -> error("Unexpected value: $value")
            }

            return if (type == DateType.DATETIME)
                "'${DEFAULT_DATE_TIME_STRING_FORMATTER.format(dateTime)}'"
            else
                "'${DEFAULT_DATE_STRING_FORMATTER.format(dateTime)}'"
        }

        override fun valueFromDB(value: Any): Any = when(value) {
            is java.util.Date -> value
            is java.sql.Date ->  java.util.Date(value.time)
            is java.sql.Timestamp -> java.util.Date(value.time)
            is Int -> java.util.Date(value.toLong())
            is Long -> java.util.Date(value)
            is String -> when {
                isSQLite && type == DateType.DATETIME -> SQLITE_DATE_TIME_STRING_FORMATTER.parse(value)
                isSQLite -> SQLITE_DATE_STRING_FORMATTER.parse(value)
                else -> value
            }
            else -> DEFAULT_DATE_TIME_STRING_FORMATTER.parse(value.toString())
        }

        override fun notNullValueToDB(value: Any): Any {
            if (value is java.util.Date) {
                val millis = value.time
                return if (type == DateType.DATETIME)
                    java.sql.Timestamp(millis)
                else {
                    java.sql.Date(millis)
                }
            }
            return value
        }
    }
}