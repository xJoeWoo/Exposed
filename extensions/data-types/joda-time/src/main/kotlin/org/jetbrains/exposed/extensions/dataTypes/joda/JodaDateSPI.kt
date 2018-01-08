package org.jetbrains.exposed.extensions.dataTypes.joda

import org.jetbrains.exposed.sql.DateApi
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.DateType
import org.jetbrains.exposed.sql.Expression
import org.joda.time.DateTime

object JodaDateSPI : DateApi<DateTime>() {
    override fun columnType(type: DateType): DateColumnType = JodaDateColumnType(type)
}

fun <T: DateTime?> Expression<T>.date() = JodaDateSPI.Date(this)
fun <T: DateTime?> Expression<T>.month() = JodaDateSPI.Month(this)
