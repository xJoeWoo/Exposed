package org.jetbrains.exposed.extensions.dataTypes.javaTime

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.tests.DatabaseTestsBase
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

object JavaTimeTable : Table() {
    val dateColumn = date("dateColumn")
    val timeColumn = datetime("timeColumn")
    val dateWithDefault = date("dateDefault").defaultExpression(JavaDateTimeSPI.CurrentDateTime())
    val timeWithDefault = datetime("timeDefault").defaultExpression(JavaDateTimeSPI.CurrentDateTime())
}

class JavaTimeMappingTest : DatabaseTestsBase() {

    @Test
    fun testMapping() {
        withTables(JavaTimeTable) {
            val isoDate = LocalDate.parse("2018-01-01", DateTimeFormatter.ISO_DATE).atTime(8, 0)
            JavaTimeTable.insert {
                it[JavaTimeTable.dateColumn] = isoDate //.parse( as LocalDateTime
                it[JavaTimeTable.timeColumn] = LocalDateTime.parse("2018-01-01T08:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }

            assertEquals(1, JavaTimeTable.select {
                JavaTimeTable.dateColumn eq JavaDateTimeSPI.Date(JavaTimeTable.timeColumn)
            }.count())

            assertEquals(1, JavaTimeTable.select {
                JavaTimeTable.dateColumn eq isoDate
            }.count())

            assertEquals(1, JavaTimeTable.select {
                JavaTimeTable.dateWithDefault eq JavaDateTimeSPI.Date(JavaDateTimeSPI.dateParam(LocalDateTime.now()))
            }.count())
        }
    }
}


