package org.jetbrains.exposed.sql.statements.api

import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.statements.StatementResult
import java.io.InputStream
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Represents a precompiled SQL statement. */
@Suppress("TooManyFunctions")
interface PreparedStatementApi {
    /** The number of result set rows that should be fetched when generated by an executed statement. */
    var fetchSize: Int?

    /** The number of seconds the JDBC driver will wait for a statement to execute. */
    var timeout: Int?

    /**
     * Sets the value for each column or expression in [args] into the appropriate statement parameter and
     * returns the number of parameters filled.
     */
    fun fillParameters(args: Iterable<Pair<IColumnType<*>, Any?>>): Int {
        args.forEachIndexed { index, (c, v) ->
            c.setParameter(this, index + 1, (c as IColumnType<Any>).valueToDB(v))
        }

        return args.count() + 1
    }

    /** Adds parameters to the statement's batch of SQL commands. */
    fun addBatch()

    /**
     * Executes an SQL query stored in a [PreparedStatement].
     *
     * @return The [ResultSet] generated by the query.
     */
    fun executeQuery(): ResultSet

    /**
     * Executes an SQL statement stored in a [PreparedStatement].
     *
     * @return The affected row count if the executed statement is a DML type;
     * otherwise, 0 if the statement returns nothing.
     */
    fun executeUpdate(): Int

    /**
     * Executes multiple SQL statements stored in a single [PreparedStatement].
     *
     * @return A list of [StatementResult]s retrieved from the database, which may store either affected row counts
     * or [ResultSet]s. The order of elements is based on the order of the statements in the `PreparedStatement`.
     */
    fun executeMultiple(): List<StatementResult>

    /** The [ResultSet] object generated by the executed statement, or `null` if none was retrieved. */
    val resultSet: ResultSet?

    operator fun set(index: Int, value: Any)

    /** Sets the statement parameter at the [index] position to SQL NULL, if allowed wih the specified [columnType]. */
    fun setNull(index: Int, columnType: IColumnType<*>)

    /**
     * Sets the statement parameter at the [index] position to the provided [inputStream],
     * either directly as a BLOB if `setAsBlobObject` is `true` or as determined by the driver.
     */
    fun setInputStream(index: Int, inputStream: InputStream, setAsBlobObject: Boolean)

    /** Sets the statement parameter at the [index] position to the provided [array] of SQL [type]. */
    fun setArray(index: Int, type: String, array: Array<*>)

    /** Closes the statement, if still open, and releases any of its database and/or driver resources. */
    fun closeIfPossible()

    /**
     * Executes batched SQL statements stored as a [PreparedStatement].
     *
     * @return A list of the affected row counts, with one element for each statement,
     * ordered based on the order in which statements were provided to the batch.
     */
    fun executeBatch(): List<Int>

    /** Cancels the statement, if supported by the database. */
    fun cancel()
}
