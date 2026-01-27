package chapter_8

import AbstractPostgreSqlTest
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import withTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

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
