package chapter_2

import AbstractPostgreSqlTest
import CityTable
import Point
import WeatherTable
import insertDummyData
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import kotlin.test.Test
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import withTransaction

// https://www.postgresql.org/docs/current/tutorial-join.html
class Chapter2_6 : AbstractPostgreSqlTest() {

    @Test
    fun `Joins Between Tables`() = withTransaction {
        SchemaUtils.create(WeatherTable)
        SchemaUtils.create(CityTable)
        WeatherTable.insertDummyData()

        CityTable.insert {
            it[name] = "San Francisco"
            it[location] = Point(-194f, 53f)
        }

        // There is no result row for the city of Hayward.
        // This is because there is no matching entry in the cities table for Hayward, so the join ignores the unmatched rows in the weather table.
        WeatherTable.join(
            CityTable,
            JoinType.INNER,
            additionalConstraint = { WeatherTable.city eq CityTable.name }
        )
            .selectAll().forEach {
                it.printRow()
            }


        WeatherTable.join(
            CityTable,
            JoinType.LEFT,
            additionalConstraint = { WeatherTable.city eq CityTable.name }
        )
            .selectAll().forEach {
                it.printRow()
            }

    }
}

private fun ResultRow.printRow() {
    println("${this[WeatherTable.city]}, ${this[WeatherTable.date]}, ${this[WeatherTable.tempLo]} to ${this[WeatherTable.tempHi]}, ${this[CityTable.name]} ${this[CityTable.location]}")
}
