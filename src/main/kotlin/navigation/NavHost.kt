package navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import data.TableViewModel
import org.jetbrains.exposed.sql.Table
import ui.TableScreen

class NavHost(
    val navController: NavController,
    val contents: @Composable NavigationGraphBuilder.() -> Unit
) {

    @Composable
    fun build() {
        NavigationGraphBuilder().renderContents()
    }

    inner class NavigationGraphBuilder(
        val navController: NavController = this@NavHost.navController
    ) {
        @Composable
        fun renderContents() {
            this@NavHost.contents(this)
        }
    }
}


@Composable
fun NavHost.NavigationGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value == route) {
        content()
    }

}

@Composable
fun CustomNavigationHost(
    navController: NavController, table: Table, viewModel: TableViewModel
) {
    val UIState by viewModel.UIState.collectAsState()
    NavHost(navController) {
        composable(table.tableName) {
            if (!UIState.isLoading)
                TableScreen(
                    navController = navController,
                    table = UIState.currentTable,
                    rows = UIState.rows,
                    searchValues = UIState.searchValues,
                    selectedRow = UIState.selectedRow,
                    onSearchValueChange = viewModel::setSearchValue,
                    onRowSelected = {viewModel.selectRow(it)},
                    onDropdownDismissed = {viewModel.selectRow(null)},
                    upsertValues = UIState.upsertValues,
                    setUpsertValue = viewModel::setUpsertValue,
                    clearUpsertValues = viewModel::clearUpsertValues,
                    upsert = viewModel::upsert,
                    delete = viewModel::delete)
        }
    }.build()
}