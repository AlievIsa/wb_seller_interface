package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navigation.NavController
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*


@Composable
fun TableScreen(
    navController: NavController,
    table: Table,
    rows: List<ResultRow>,
    searchValues: List<String>,
    selectedRow: ResultRow?,
    onSearchValueChange: (Int, String) -> Unit,
    onRowSelected: (ResultRow) -> Unit,
    onDropdownDismissed: () -> Unit,
    upsertValues: List<String>,
    setUpsertValue: (Int, String) -> Unit,
    clearUpsertValues: () -> Unit,
    upsert: () -> Unit,
    delete: () -> Unit) {

    var isUpsertWindowOpen by remember { mutableStateOf(false) }
    var isFunctionsWindowOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Row {
                table.columns.forEachIndexed { i, column ->
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = searchValues[i],
                        onValueChange = { onSearchValueChange(i, it) },
                        label = { Text(column.name) })
                }
            }
//            Row(modifier = Modifier.border(1.dp, Color.Gray)) {
//                table.columns.forEach {
//                    Text(it.name, modifier = Modifier.border(1.dp, Color.Gray).padding(8.dp).weight(1f), fontWeight = FontWeight.Bold, maxLines = 1)
//                }
//            }
            LazyColumn {
                itemsIndexed(rows) {_, row ->
                    Row(modifier = Modifier.clickable {
                        onRowSelected(row)
                    }) {
                        table.columns.forEach {
                            Text(row.getOrNull(it).toString(), modifier = Modifier.border(1.dp, Color.Gray).padding(8.dp).weight(1f), maxLines = 1)
                        }
                        DropdownMenu(row == selectedRow, onDismissRequest = onDropdownDismissed) {
                            DropdownMenuItem(content = { Text("Update") }, onClick = {
                                table.columns.forEachIndexed { i, column ->
                                    setUpsertValue(i, selectedRow!![column].toString())
                                }
                                isUpsertWindowOpen = true
                            })
                            DropdownMenuItem(content = { Text("Delete") }, onClick = delete)
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Button(modifier = Modifier.padding(end = 16.dp), onClick = { isUpsertWindowOpen = true }) {
                Icon(imageVector = Icons.Filled.Add, null)
            }
            Button(onClick = { isFunctionsWindowOpen = true }) {
                Icon(imageVector = Icons.Filled.Info, null)
            }
        }
    }
    if (isUpsertWindowOpen) {
        UpsertWindow(
            table,
            upsertValues,
            setUpsertValue,
            upsert,
            onClose = {
                isUpsertWindowOpen = false
                clearUpsertValues()
            })
    }

    if (isFunctionsWindowOpen) {
        FunctionsWindow(onClose = {
            isFunctionsWindowOpen = false
        })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UpsertWindow(
    table: Table,
    upsertValues: List<String>,
    setUpsertValue: (Int, String) -> Unit,
    upsert: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(onDismissRequest = onClose,
        text = {
            Column(modifier = Modifier.padding(24.dp)) {
                table.columns.forEachIndexed { i, column ->
                    OutlinedTextField(
                        value = upsertValues[i],
                        onValueChange = { setUpsertValue(i, it) },
                        label = { Text(column.name) })
                }
            }
        }, confirmButton = {
            Button(onClick = {
                println(upsertValues[0])
                upsert()
                onClose()
            }) {
                Text("Apply")
            }
        })

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FunctionsWindow(onClose: () -> Unit) {

    var result by remember { mutableStateOf(0) }
    var productId by remember { mutableStateOf("") }
    var batchId by remember { mutableStateOf("") }
    var productCardId by remember { mutableStateOf("") }

    AlertDialog(modifier = Modifier.fillMaxSize(), onDismissRequest = onClose,
        title = { Text("Functions and procedures", modifier = Modifier.padding(top = 16.dp)) },
        text = {
            Column(modifier = Modifier.padding(top = 32.dp)) {
                Row {
                    OutlinedTextField(
                        modifier = Modifier.width(110.dp),
                        value = productId,
                        onValueChange = { productId = it },
                        label = { Text("id_товара")}
                    )
                    OutlinedTextField(
                        modifier = Modifier.width(110.dp),
                        value = batchId,
                        onValueChange = { batchId = it },
                        label = { Text("id_партии")}
                    )
                    Button(
                        modifier = Modifier.padding(14.dp).align(Alignment.CenterVertically),
                        onClick = {
                            result = calculateRevenue(productId.toInt(),
                                batchId.toInt()) })
                    {
                        Text("Рассчитать\nвыручку")
                    }
                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            result = calculateProfit(productId.toInt(),
                                batchId.toInt()) })
                    {
                        Text("Рассчитать\nприбыль")
                    }
                    Text(
                        result.toString(),
                        modifier = Modifier.padding(start = 32.dp).align(Alignment.CenterVertically),
                        fontSize = 24.sp)
                }
                Row {
                    OutlinedTextField(
                        modifier = Modifier.width(120.dp).align(Alignment.CenterVertically),
                        value = productCardId,
                        onValueChange = { productCardId = it },
                        label = { Text("id_карточки")}
                    )
                    Button(modifier = Modifier.padding(14.dp).align(Alignment.CenterVertically), onClick = { calculateMinPrice(productCardId.toInt()) }) { Text("Рассчитать\nминимальную\nцену") }
                }
            }
        }, buttons = {})
}

private fun calculateRevenue(productId: Int, batchId: Int): Int {
    val price = transaction {
        ProductCard.slice(ProductCard.price)
            .select { ProductCard.productId eq productId }
            .singleOrNull()
            ?.get(ProductCard.price) ?: 0
    }
    val amount = transaction {
        Batch.slice(Batch.productAmount)
            .select { (Batch.batchId eq batchId) and (Batch.productId eq productId) }
            .singleOrNull()
            ?.get(Batch.productAmount) ?: 0
    }
    return price * amount
}

private fun calculateProfit(productId: Int, batchId: Int): Int {
    val price = transaction {
        ProductCard.slice(ProductCard.price)
            .select { ProductCard.productId eq productId }
            .singleOrNull()?.get(ProductCard.price) ?: 0
    }

    val productCost = transaction {
        Product.slice(Product.productCost)
            .select { Product.productId eq productId }
            .singleOrNull()?.get(Product.productCost) ?: 0
    }

    val logisticsPrice = transaction {
        (Batch innerJoin Delivery innerJoin Storage).slice(Storage.logisticPrice)
            .select {
                (Batch.batchId eq batchId) and
                        (Batch.productId eq productId) and
                        (Batch.deliveryId eq Delivery.deliveryId) and
                        (Delivery.storageId eq Storage.storageId) and
                        (Batch.batchId eq batchId) and
                        (Batch.productId eq productId)
            }
            .singleOrNull()?.get(Storage.logisticPrice) ?: 0.0
    }

    val amount = transaction {
        Batch.slice(Batch.productAmount)
            .select { (Batch.batchId eq batchId) and (Batch.productId eq productId) }
            .singleOrNull()?.get(Batch.productAmount) ?: 0
    }
    return (price * amount * 0.85 - (productCost + logisticsPrice) * amount).toInt()
}

private fun calculateMinPrice(productCardId: Int) {
    transaction {

        val productCost = (ProductCard innerJoin Product)
            .slice(Product.productCost)
            .select { ProductCard.productCardId eq productCardId }
            .singleOrNull()?.get(Product.productCost) ?: return@transaction

        val newPrice = when {
            productCost in 0..1000 -> productCost * 3
            productCost in 1001..2000 -> productCost * 2.5
            productCost > 2000 -> productCost * 2
            else -> productCost
        }

        ProductCard.update({ ProductCard.productCardId eq productCardId }) {
            it[price] = newPrice.toInt()
        }
    }
}