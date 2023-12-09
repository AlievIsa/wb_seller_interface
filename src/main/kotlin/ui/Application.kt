package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import data.TableViewModel
import navigation.CustomNavigationHost
import navigation.rememberNavController
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*

@Composable
fun Application(viewModel: TableViewModel, tables: Array<Table>) {
    val table = mutableStateOf(tables[0])

    val navController by rememberNavController(tables[0].tableName)
    val currentScreen by remember {
        navController.currentScreen
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.background(color = MaterialTheme.colors.background)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    tables.forEach {
                        NavigationRailItem(
                            selected = currentScreen == it.tableName,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.List,
                                    contentDescription = it.tableName
                                )
                            },
                            label = {
                                Text(it.tableName)
                            },
                            onClick = {
                                table.value = it
                                viewModel.setTable(it)
                                navController.navigate(it.tableName)
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CustomNavigationHost(navController = navController, table.value, viewModel)
                }
            }
        }
    }
}