package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ProductCard: Table("карточка") {
    val productCardId: Column<Int> = integer("id_карточки")
    override val primaryKey: PrimaryKey = PrimaryKey(productCardId)

    val categoryId: Column<Int> = integer("id_категории").references(Category.categoryId)
    val productId: Column<Int> = integer("id_товара").references(Product.productId)
    val price: Column<Int> = integer("цена")
    val vendorCode: Column<Int> = integer("артикул")
    val photoAmount: Column<Int> = integer("количество_фото")
    val videoURI: Column<String> = varchar("uri_видео", length = 120)
}