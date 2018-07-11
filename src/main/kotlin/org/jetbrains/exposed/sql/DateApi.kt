package org.jetbrains.exposed.sql

import org.jetbrains.exposed.sql.DefaultDateSPI.columnType
import org.jetbrains.exposed.sql.vendors.SQLiteDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.text.SimpleDateFormat
import java.util.*

enum class DateType {
    INSTANT, DATETIME, DATE, LOCAL_DATETIME, TIME
}

interface DateExpression<T>

abstract class DateFunction<DATE>(type: DateType) : Function<DATE>(columnType(type)), DateExpression<DATE>

class DateColumn<T>(table: Table, name: String, columnType: IColumnType) : Column<T>(table, name, columnType), DateExpression<T>

abstract class DateColumnType(val type: DateType): ColumnType() {
    override fun sqlType(): String  = currentDialect.dataTypeProvider.dateTimeType(type)
    protected val isSQLite: Boolean get() = currentDialect is SQLiteDialect
}

abstract class DateApi<LOCALDATE, LOCALDATETIME, DATETIME, INSTANT> {
    inner class Date<D, T>(val expr: T) : DateFunction<LOCALDATE?>(DateType.DATE) where T : DateExpression<D>, T : Expression<D> {
        override fun toSQL(queryBuilder: QueryBuilder): String
                = currentDialect.functionProvider.cast(expr, columnType, queryBuilder)
    }

    inner class CurrentDateTime<D> : DateFunction<D>(DateType.DATETIME) {
        override fun toSQL(queryBuilder: QueryBuilder) = "CURRENT_TIMESTAMP"
    }

    inner class Month<D, T>(val expr: T) : Function<Int>(columnType(DateType.DATE)) where T : DateExpression<D>, T : Expression<D>  {
        override fun toSQL(queryBuilder: QueryBuilder): String = "MONTH(${expr.toSQL(queryBuilder)})"
    }

    abstract fun columnType(type: DateType) : DateColumnType

    @Suppress("UNCHECKED_CAST")
    fun dateParam(value: LOCALDATE) = object : DateFunction<LOCALDATE>(DateType.DATE) {
        override fun toSQL(queryBuilder: QueryBuilder) = queryBuilder.registerArgument(columnType, value)
    }

    fun dateTimeParam(value: DATETIME) = object : DateFunction<DATETIME>(DateType.DATE) {
        override fun toSQL(queryBuilder: QueryBuilder) = queryBuilder.registerArgument(columnType, value)
    }

    fun dateLiteral(value: LOCALDATE) : LiteralOp<LOCALDATE> = LiteralOp(columnType(DateType.DATE), value)
    fun dateTimeLiteral(value: DATETIME) : LiteralOp<DATETIME> = LiteralOp(columnType(DateType.DATETIME), value)
}

object DefaultDateSPI : DateApi<Date, Date, Date, Date>() {
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
                else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
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