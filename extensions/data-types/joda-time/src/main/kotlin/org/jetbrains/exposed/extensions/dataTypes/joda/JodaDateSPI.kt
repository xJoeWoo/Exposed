package org.jetbrains.exposed.extensions.dataTypes.joda

import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.time.Instant

typealias JodaDateApi = DateApi<LocalDate, Instant, DateTime>

object JodaDateSPI : JodaDateApi() {
    override fun columnType(type: DateType): DateColumnType = JodaDateColumnType(type)
}

fun <D, T> T.date() : DateFunction<LocalDate?> where T : DateExpression<D>, T : Expression<D>
        = JodaDateSPI.Date(this)

fun <D, T>  T.month() where T : DateExpression<D>, T : Expression<D>
        = JodaDateSPI.Month(this)

