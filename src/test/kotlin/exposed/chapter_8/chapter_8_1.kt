package exposed.chapter_8

import exposed.AbstractPostgreSqlTest
import exposed.withTransaction
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import kotlin.test.Test

// https://www.postgresql.org/docs/18/datatype-numeric.html
class chapter_8_1 : AbstractPostgreSqlTest() {
    @Test
    fun `Integer Types #`() =
        withTransaction {
            val t1 =
                object : Table("t1") {
                    val integer = integer("integer")

                    // 4 byte
                    val uinteger = uinteger("uinteger")
                }
            SchemaUtils.create(t1)

            exec("SELECT to_tsvector('integer uinteger') @@ to_tsquery('fat & rat');")
        }
}
