import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.TableViewModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*
import ui.Application

fun main() = application {
    Database.connect(
        url = "jdbc:mysql://localhost:3307/wb_seller",
        user = "root",
        password = "Dadochka95",
        driver = "com.mysql.cj.jdbc.Driver"
    )

    val tables = arrayOf(
        Batch,
        Category,
        Delivery,
        Photo,
        Product,
        ProductCard,
        Storage
    )

    transaction {
        SchemaUtils.create(tables = tables)
    }

    val viewModel = TableViewModel()

    Window(onCloseRequest = ::exitApplication, title = "Интерфейс базы данных | Алиев Иса") {
        Application(viewModel, tables)
    }
}
