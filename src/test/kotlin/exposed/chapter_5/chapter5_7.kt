package exposed.chapter_5

import exposed.AbstractPostgreSqlTest
import exposed.withTransaction
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import kotlin.test.Test

// https://www.postgresql.org/docs/18/ddl-alter.html
class chapter5_7 : AbstractPostgreSqlTest() {
    @Test
    fun `Adding a Column`() =
        withTransaction {
            SchemaUtils.create(TestTable)

            exec(
                """
                ALTER TABLE test ADD COLUMN foo TEXT;
                """.trimIndent(),
            )
        }

    @Test
    fun `Removing a Column`() =
        withTransaction {
            SchemaUtils.create(TestTable)

            exec(
                """
                ALTER TABLE test DROP COLUMN count;
                """.trimIndent(),
            )
        }
}

object TestTable : Table("test") {
    val id = integer("id")
    val count = integer("count")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
