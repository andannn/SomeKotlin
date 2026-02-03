package exposed.chapter_7

import exposed.AbstractPostgreSqlTest
import exposed.withTransaction
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// https://www.postgresql.org/docs/18/queries-union.html
class chapter_7_4 : AbstractPostgreSqlTest() {
    @Test
    fun `Combining Queries (UNION, INTERSECT, EXCEPT)`() =
        withTransaction {
            val t1 =
                object : Table("t1") {
                    val id = integer("id")
                    val name = varchar("name", 80)
                }
            val t2 =
                object : Table("t2") {
                    val id = integer("id")
                    val name = varchar("name", 80)
                }

            SchemaUtils.create(t1, t2)

            t1.insert {
                it[id] = 1
                it[name] = "Alice"
            }

            t1.insert {
                it[id] = 1
                it[name] = "Bob"
            }

            t2.insert {
                it[id] = 1
                it[name] = "Bob"
            }

            t2.insert {
                it[id] = 10
                it[name] = "Foo"
            }

            //  it eliminates duplicate rows from its result, in the same way as DISTINCT
            t1
                .selectAll()
                .union(
                    t2.selectAll(),
                ).iterator()
                .let {
                    assertEquals("Alice", it.next()[t1.name])
                    assertEquals("Bob", it.next()[t1.name])
                    assertEquals("Foo", it.next()[t1.name])
                    assertFalse(it.hasNext())
                }
            // Union all return all row
            t1
                .selectAll()
                .unionAll(
                    t2.selectAll(),
                ).iterator()
                .let {
                    assertEquals("Alice", it.next()[t1.name])
                    assertEquals("Bob", it.next()[t1.name])
                    assertEquals("Bob", it.next()[t1.name])
                    assertEquals("Foo", it.next()[t1.name])
                    assertFalse(it.hasNext())
                }

            // INTERSECT returns all rows that are both in the result of query1 and in the result of query2.
            t1
                .selectAll()
                .intersect(t2.selectAll())
                .iterator()
                .let {
                    assertEquals("Bob", it.next()[t1.name])
                    assertFalse(it.hasNext())
                }

            // EXCEPT returns all rows that are in the result of query1 but not in the result of query2.
            t1
                .selectAll()
                .except(t2.selectAll())
                .iterator()
                .let {
                    assertEquals("Alice", it.next()[t1.name])
                    assertFalse(it.hasNext())
                }
        }
}
