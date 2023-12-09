package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Photo: Table("фото") {
    val photoId: Column<Int> = integer("id_фото")
    override val primaryKey: PrimaryKey = PrimaryKey(photoId)

    val productCardId: Column<Int> = integer("id_карточки").references(ProductCard.productCardId)
    val photoURI: Column<String> = varchar("uri_фото", length = 120)
}