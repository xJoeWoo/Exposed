package org.jetbrains.exposed.extensions.dataTypes.javaTime

import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

typealias JavaDateApi = DateApi<LocalDate, LocalDateTime, LocalDateTime, Instant>

object JavaDateTimeSPI : JavaDateApi() {
    override fun columnType(type: DateType): DateColumnType = Java8ColumnType(type)
}

fun <D, T> T.date() : DateFunction<LocalDate?> where T : DateExpression<D>, T : Expression<D>
        = JavaDateTimeSPI.Date(this)

fun <D, T>  T.month() where T : DateExpression<D>, T : Expression<D>
        = JavaDateTimeSPI.Month(this)
