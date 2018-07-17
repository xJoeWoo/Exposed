package org.jetbrains.exposed.extensions.dataTypes.joda

import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.time.Instant

typealias JodaDateApi = DateApi<LocalDate, LocalDateTime, DateTime, Instant>

object JodaDateSPI : JodaDateApi() {
    override fun columnType(type: DateType): DateColumnType = JodaDateColumnType(type)
}

@Deprecated("Use date() instead", replaceWith = ReplaceWith("date()"))
fun <T:DateTime?> Expression<T>.deprecatedDate() : DateFunction<DateTime>
        = castTo<DateTime>(JodaDateColumnType(DateType.DATETIME)) as DateFunction<DateTime>

fun <D, T> T.date() : DateFunction<LocalDate?> where T : DateExpression<D>, T : Expression<D>
        = JodaDateSPI.Date(this)

fun <D, T>  T.month() where T : DateExpression<D>, T : Expression<D>
        = JodaDateSPI.Month(this)

