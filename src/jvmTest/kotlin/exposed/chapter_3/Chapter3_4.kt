package exposed.chapter_3

import exposed.AbstractPostgreSqlTest
import exposed.withTransaction
import kotlin.test.Test

// https://www.postgresql.org/docs/current/tutorial-transactions.html
class Chapter3_4 : AbstractPostgreSqlTest() {
    @Test
    fun transactions() =
        withTransaction {
        }
}
