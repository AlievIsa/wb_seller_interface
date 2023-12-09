package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Batch: Table("партия") {
    val batchId: Column<Int> = integer("id_партии")
    val productId: Column<Int> = integer("id_товара").references(Product.productId)
    override val primaryKey: PrimaryKey = PrimaryKey(batchId, productId)

    val deliveryId: Column<Int> = integer("id_поставки").references(Delivery.deliveryId)
    val productAmount: Column<Int> = integer("количество")
}