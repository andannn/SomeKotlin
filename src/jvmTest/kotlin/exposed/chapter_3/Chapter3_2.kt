package exposed.chapter_3

import exposed.AbstractPostgreSqlTest
import exposed.CityTable
import exposed.Point
import exposed.WeatherTable
import exposed.insertDummyData
import exposed.point
import exposed.withTransaction
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.test.Test

// https://www.postgresql.org/docs/current/tutorial-views.html
class Chapter3_2 : AbstractPostgreSqlTest() {
    @Test
    fun Deletions() =
        withTransaction {
            SchemaUtils.create(WeatherTable, CityTable)
            exec(
                """
                CREATE VIEW myview AS
                    SELECT name, temp_lo, temp_hi, prcp, date, location
                        FROM weather, cities
                        WHERE city = name;
                """.trimIndent(),
            )

            WeatherTable.insertDummyData()
            CityTable.insert {
                it[name] = "San Francisco"
                it[location] = Point(-194f, 53f)
            }

            Myview
                .selectAll()
                .forEach {
                    it.printRow()
                }
        }
}

private fun ResultRow.printRow() {
    println("${this[Myview.name]}, ${this[Myview.date]}, ${this[Myview.tempLo]} to ${this[Myview.tempHi]}, ${this[Myview.location]}")
}

private object Myview : Table("myview") {
    val tempLo = integer("temp_lo")
    val tempHi = integer("temp_hi")
    val prcp = float("prcp")
    val date = date("date")
    val name = varchar("name", 80)
    val location = point("location")
}
