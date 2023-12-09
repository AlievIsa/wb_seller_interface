package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Storage: Table("склад") {
    val storageId: Column<Int> = integer("id_склада")
    override val primaryKey: PrimaryKey = PrimaryKey(storageId)

    val address: Column<String> = varchar("адрес", length = 120)
    val logisticPrice: Column<Double> = double("цена_логистики")
    val storagePrice: Column<Double> = double("цена_хранения")
}