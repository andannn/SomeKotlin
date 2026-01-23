package chapter_3

import AbstractPostgreSqlTest
import Point
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import point
import withTransaction
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFails

// https://www.postgresql.org/docs/current/tutorial-fk.html
class Chapter3_3 : AbstractPostgreSqlTest() {
    @Test
    fun `Foreign Keys`() =
        withTransaction {
            SchemaUtils.create(MyWeatherTable, MyCityTable)

            MyCityTable.insert {
                it[name] = "San Francisco"
                it[location] = Point(-194f, 53f)
            }

            // Referential integrity constraint violation: "fk_weather_city__name: public.weather FOREIGN KEY(city) REFERENCES public.cities(name) ('Hayward')";
            assertFails {
                MyWeatherTable.insert {
                    it[city] = "Hayward"
                    it[tempLo] = 37
                    it[tempHi] = 54
                    it[prcp] = 0f
                    it[date] = LocalDate.of(1994, 11, 29)
                }
            }
        }
}

private object MyWeatherTable : Table("weather") {
    val city = reference("city", MyCityTable.name)

    val tempLo = integer("temp_lo")
    val tempHi = integer("temp_hi")
    val prcp = float("prcp")
    val date = date("date")
}

private object MyCityTable : Table("cities") {
    val name = varchar("name", 80)
    val location = point("location")

    override val primaryKey = PrimaryKey(name)
}
