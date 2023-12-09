package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Product: Table("товар") {
    val productId: Column<Int> = integer("id_товара")
    override val primaryKey: PrimaryKey = PrimaryKey(productId)

    val supplierTIN: Column<Int> = integer("ИНН_поставщика")
    val productName: Column<String> = varchar("название_товара", length = 20)
    val productType: Column<String> = varchar("тип_товара", length = 20)
    val productCost: Column<Int> = integer("цена_закупки")
}