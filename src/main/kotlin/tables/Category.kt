package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Category: Table("категория") {
    val categoryId: Column<Int> = integer("id_категории")
    override val primaryKey: PrimaryKey = PrimaryKey(categoryId)

    val productAmount: Column<Int> = integer("количество_товара")
    val categoryName: Column<String> = varchar("название_категории", length = 40)
}