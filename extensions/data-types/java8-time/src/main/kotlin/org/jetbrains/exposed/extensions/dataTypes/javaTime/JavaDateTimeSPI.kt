package org.jetbrains.exposed.extensions.dataTypes.javaTime

import org.jetbrains.exposed.sql.DateApi
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.DateType
import org.jetbrains.exposed.sql.Expression
import java.time.LocalDateTime

object JavaDateTimeSPI : DateApi<LocalDateTime>() {
    override fun columnType(type: DateType): DateColumnType = JavaLocalDateColumnType(type)
}

fun <T: LocalDateTime?> Expression<T>.date() = JavaDateTimeSPI.Date(this)
fun <T: LocalDateTime?> Expression<T>.month() = JavaDateTimeSPI.Month(this)
