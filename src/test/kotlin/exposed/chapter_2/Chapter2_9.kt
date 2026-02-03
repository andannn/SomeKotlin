package exposed.chapter_2

import exposed.AbstractPostgreSqlTest
import exposed.WeatherTable
import exposed.insertDummyData
import exposed.withTransaction
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.test.Test

// https://www.postgresql.org/docs/current/tutorial-delete.html
class Chapter2_9 : AbstractPostgreSqlTest() {
    @Test
    fun Deletions() =
        withTransaction {
            SchemaUtils.create(WeatherTable)
            WeatherTable.insertDummyData()

            WeatherTable.selectAll().forEach { it.printRow() }
            WeatherTable.deleteWhere {
                WeatherTable.city eq "Hayward"
            }
            WeatherTable.selectAll().forEach { it.printRow() }
        }
}

private fun ResultRow.printRow() {
    println(
        "city: ${this[WeatherTable.city]}, tempLo: ${this[WeatherTable.tempLo]}, tempHi: ${this[WeatherTable.tempHi]}, prcp: ${this[WeatherTable.prcp]}, date: ${this[WeatherTable.date]}",
    )
}
