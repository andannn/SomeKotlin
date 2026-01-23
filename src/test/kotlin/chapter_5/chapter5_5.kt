package chapter_5

import AbstractPostgreSqlTest
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import withTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

// https://www.postgresql.org/docs/18/ddl-constraints.html
class chapter5_5 : AbstractPostgreSqlTest() {
    @Test
    fun `Check Constraints`() =
        withTransaction {
            val testTable =
                object : Table("test") {
                    val price = float("price").check { it greaterEq 0f }
                }
            SchemaUtils.create(testTable)

            testTable.insert {
                it[price] = 1.0f
            }

            assertFails {
                testTable.insert {
                    it[price] = -1.0f
                }
            }
        }

    @Test
    fun `Check Constraints with multiple`() =
        withTransaction {
            val testTable =
                object : Table("test2") {
                    val price = float("price").check { it greaterEq 0f }
                    val discountedPrice =
                        float("discounted_price")
                            .check { it greater 0f }
                            .check { it less price }
                }
            SchemaUtils.create(testTable)

            testTable.insert {
                it[price] = 1.0f
                it[discountedPrice] = 0.9f
            }

            assertFails {
                testTable.insert {
                    it[price] = 1.0f
                    it[discountedPrice] = -1f
                }
            }

            assertFails {
                testTable.insert {
                    it[price] = 1.0f
                    it[discountedPrice] = 1.0f
                }
            }
        }

    @Test
    fun `Unique Constraints`() =
        withTransaction {
            val testTable =
                object : Table("test3") {
                    val id = integer("price").uniqueIndex()
                }

            SchemaUtils.create(testTable)

            testTable.insert { it[id] = 1 }

            assertFails {
                testTable.insert { it[id] = 1 }
            }
        }

    // NULL can be insert multiple times with unique constrains
    @Test
    fun `Unique Null Constraints`() =
        withTransaction {
            val table =
                object : Table("test4") {
                    val id = integer("price").nullable().uniqueIndex()
                }
            SchemaUtils.create(table)

            table.insert { it[id] = null }
            table.insert { it[id] = null }
        }

    // NULL can not insert multiple times with unique constrains
    @Test
    fun `UNIQUE NULLS NOT DISTINCT Constraints`() =
        withTransaction {
            val testTable =
                object : Table("test5") {
                    val id =
                        integer("price")
                            .nullable()
                            .withDefinition("UNIQUE NULLS NOT DISTINCT")
                }

            SchemaUtils.create(testTable)

            testTable.insert { it[id] = null }
            assertFails {
                testTable.insert { it[id] = null }
            }
        }

    // https://www.postgresql.org/docs/18/ddl-constraints.html#DDL-CONSTRAINTS-PRIMARY-KEYS
    @Test
    fun `PRIMARY KEY Constraints`() =
        withTransaction {
            val testTable =
                object : Table("test6") {
                    val id = integer("id")

                    override val primaryKey = PrimaryKey(id)
                }
            SchemaUtils.create(testTable)

            testTable.insert { it[id] = 1 }
        }

    @Test
    fun `Foreign Keys`() =
        withTransaction {
            val productTable =
                object : Table("productTable") {
                    val productNum = integer("productNum")

                    override val primaryKey: PrimaryKey = PrimaryKey(productNum)

                    val name = varchar("name", 100)
                }

            val orderTable =
                object : Table("orderTable") {
                    val orderNum = integer("orderNum")

                    val productNum =
                        integer("productNum")
                            .references(productTable.productNum)

                    override val primaryKey: PrimaryKey = PrimaryKey(orderNum)
                }

            SchemaUtils.create(productTable)
            SchemaUtils.create(orderTable)

            // It is impossible to create orders with non-NULL product_no entries that do not appear in the products table.
            assertFails {
                orderTable.insert {
                    it[orderNum] = 1
                    it[productNum] = 2
                }
            }

            productTable.insert {
                it[productNum] = 1
                it[name] = "product1"
            }

            orderTable.insert {
                it[orderNum] = 1
                it[productNum] = 1
            }

            // delete referenced item will fail
            assertFails {
                productTable.deleteWhere {
                    productTable.productNum eq 1
                }
            }
        }

    @Test
    fun `Foreign Keys - many to many`() =
        withTransaction {
            val productTable =
                object : Table("productTable") {
                    val productNum = integer("productNum")

                    override val primaryKey: PrimaryKey = PrimaryKey(productNum)

                    val name = varchar("name", 100)
                }

            val orderTable =
                object : Table("orderTable") {
                    val orderNum = integer("orderNum")

                    override val primaryKey: PrimaryKey = PrimaryKey(orderNum)
                }

            val orderItems =
                object : Table("orderItems") {
                    val orderNum =
                        integer("orderNum")
                            .references(orderTable.orderNum, onDelete = ReferenceOption.CASCADE)

                    val productNum =
                        integer("productNum")
                            .references(productTable.productNum)
                }

            SchemaUtils.create(productTable)
            SchemaUtils.create(orderTable)
            SchemaUtils.create(orderItems)

            productTable.insert {
                it[productNum] = 1
                it[name] = "product1"
            }

            orderTable.insert {
                it[orderNum] = 10
            }

            orderItems.insert {
                it[orderNum] = 10
                it[productNum] = 1
            }

            // productTable.productNum is referenced by ON DELETE RESTRICT
            assertFails {
                productTable.deleteWhere {
                    productTable.productNum eq 1
                }
            }

            // orderTable.orderNum is referenced by ON DELETE CASCADE
            orderTable.deleteWhere {
                orderTable.orderNum eq 10
            }
            assertEquals(0, orderItems.selectAll().count())
        }

//    @Test
//    fun `Foreign Keys - many to many2`() =
//        withTransaction {
//            val tenants =
//                object : Table("tenants") {
//                    val tenantId = integer("tenant_id")
//
//                    override val primaryKey = PrimaryKey(tenantId)
//                }
//
//            val users =
//                object : Table("users") {
//                    // 级联删除租户下的用户
//                    val tenantId =
//                        integer("tenant_id")
//                            .references(tenants.tenantId, onDelete = ReferenceOption.CASCADE)
//                    val userId = integer("user_id")
//
//                    // 定义复合主键 (tenant_id, user_id)
//                    override val primaryKey = PrimaryKey(tenantId, userId)
//                }
//
//            val posts =
//                object : Table("posts") {
//                    val tenantId =
//                        integer("tenant_id")
//                            .references(tenants.tenantId, onDelete = ReferenceOption.CASCADE)
//                    val postId = integer("post_id")
//
//                    val authorId = integer("author_id").nullable()
//
//                    override val primaryKey = PrimaryKey(tenantId, postId)
//                }
//            SchemaUtils.create(
//                tenants,
//                users,
//                posts,
//            )
//            exec(
//                """
//                ALTER TABLE posts
//                ADD CONSTRAINT fk_custom
//                FOREIGN KEY (tenant_id, author_id)
//                REFERENCES users (tenant_id, user_id)
//                ON DELETE SET NULL (author_id)
//                """.trimIndent(),
//            )
//
//            tenants.insert {
//                it[tenantId] = 1
//            }
//
//            users.insert {
//                it[tenantId] = 1
//                it[userId] = 10
//            }
//        }
}
