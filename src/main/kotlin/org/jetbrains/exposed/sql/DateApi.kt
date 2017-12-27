package org.jetbrains.exposed.sql

import org.jetbrains.exposed.sql.vendors.SQLiteDialect
import org.jetbrains.exposed.sql.vendors.currentDialect

abstract class DateColumnType(val time: Boolean): ColumnType() {
    override fun sqlType(): String  = if (time) currentDialect.dataTypeProvider.dateTimeType() else "DATE"
    protected val isSQLite: Boolean get() = currentDialect is SQLiteDialect
}

abstract class DateApi<T:Any> {
    inner class Date(val expr: Expression<T?>) : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder): String = "DATE(${expr.toSQL(queryBuilder)})"
        override val columnType: IColumnType = columnType(false)
    }

    inner class CurrentDateTime() : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder) = "CURRENT_TIMESTAMP"
        override val columnType: IColumnType = columnType(true)
    }

    inner class Month(val expr: Expression<T?>) : Function<T>() {
        override fun toSQL(queryBuilder: QueryBuilder): String = "MONTH(${expr.toSQL(queryBuilder)})"
        override val columnType: IColumnType = columnType(false)
    }

    abstract protected fun columnType(time: Boolean) : DateColumnType

    fun dateParam(value: T): Expression<T> = QueryParameter(value, columnType(false))
    fun dateTimeParam(value: T): Expression<T> = QueryParameter(value, columnType(true))

    fun dateLiteral(value: T) : LiteralOp<T> = LiteralOp(columnType(false), value)
    fun dateTimeLiteral(value: T) : LiteralOp<T> = LiteralOp(columnType(true), value)
}