package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Delivery: Table("поставка") {
    val deliveryId: Column<Int> = integer("id_поставки")
    override val primaryKey: PrimaryKey = PrimaryKey(deliveryId)

    val storageId: Column<Int> = integer("id_склада").references(Storage.storageId)
    val status: Column<String> = varchar("статус", length = 20)
}