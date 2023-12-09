package data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*


val tables = arrayOf(
    Batch,
    Category,
    Delivery,
    Photo,
    Product,
    ProductCard,
    Storage
)

class TableViewModel {
    private var _UIState = MutableStateFlow(TableUIState())
    val UIState = _UIState.asStateFlow()
    val viewModelScope = CoroutineScope(SupervisorJob())

    init {
        clearSearchValues()
        clearUpsertValues()
        updateTable()
    }

    private fun updateTable() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _UIState.update { state ->
                    var newList = transaction {
                        state.currentTable.selectAll().toList()
                    }
                    if (state.searchValues.any { it.isNotEmpty() }) {
                        state.searchValues.forEachIndexed { i, value ->
                            if(value.isNotEmpty()) {
                                newList = newList.filter { it[state.currentTable.columns[i]].toString().contains(value) }
                            }
                        }
                    }
                    state.copy(rows = newList, isLoading = false)
                }
            }
        }
    }

    fun setTable(table: Table) {
        _UIState.update { state ->
            state.copy(currentTable = table, isLoading = true)
        }
        updateTable()
        clearSearchValues()
        clearUpsertValues()
    }

    fun selectRow(row: ResultRow?) {
        _UIState.update { state ->
            state.copy(selectedRow = row)
        }
    }

    fun setSearchValue(index: Int, value: String) {
        _UIState.update { state ->
            val newList = state.searchValues.toMutableList()
            newList[index] = value
            state.copy(searchValues = newList)
        }
        updateTable()
    }

    private fun clearSearchValues() {
        _UIState.update { state ->
            state.copy(searchValues = List(state.currentTable.columns.size) {""})
        }
    }

    fun setUpsertValue(index: Int, value: String) {
        _UIState.update { state ->
            val newList = state.upsertValues.toMutableList()
            newList[index] = value
            state.copy(upsertValues = newList)
        }
        updateTable()
    }

    fun clearUpsertValues() {
        _UIState.update { state ->
            state.copy(upsertValues = List(state.currentTable.columns.size) {""})
        }
    }

    fun upsert() {
        val stringValues = mutableListOf<String>()
        UIState.value.currentTable.columns.forEachIndexed { i, _ ->
            stringValues.add(UIState.value.upsertValues[i])
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                transaction {
                    UIState.value.currentTable.upsert { statement ->
                        this.columns.forEachIndexed { i, column ->
                            val stringValue = stringValues[i]
                            @Suppress("UNCHECKED_CAST")
                            when (column.columnType) {
                                is IntegerColumnType -> statement[column as Column<Int>] = stringValue.toInt()
                                is DoubleColumnType -> statement[column as Column<Double>] = stringValue.toDouble()
                                is VarCharColumnType -> statement[column as Column<String>] = stringValue
                                else -> {
                                    throw Exception("${column.columnType}")
                                }
                            }
                        }
                    }
                }
            }
            updateTable()
        }
    }


    fun delete() {
        viewModelScope.launch {
            UIState.value.currentTable.let {table ->
                val firstPrimaryKeyColumn = table.primaryKey!!.columns.first()
                when (firstPrimaryKeyColumn.columnType) {
                    is StringColumnType -> transaction {
                        table.deleteWhere {
                            firstPrimaryKeyColumn as Column<String> eq UIState.value.selectedRow!![firstPrimaryKeyColumn]
                        }
                    }
                    is IntegerColumnType -> transaction {
                        table.deleteWhere {
                            firstPrimaryKeyColumn as Column<Int> eq UIState.value.selectedRow!![firstPrimaryKeyColumn]
                        }
                    }
                    is AutoIncColumnType -> transaction {
                        table.deleteWhere {
                            firstPrimaryKeyColumn as Column<Int> eq UIState.value.selectedRow!![firstPrimaryKeyColumn]
                        }
                    }
                    else -> {}
                }
            }
            updateTable()
        }
    }
}

data class TableUIState(
    val currentTable: Table = tables.first(),
    val rows: List<ResultRow> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRow: ResultRow? = null,
    val searchValues: List<String> = emptyList(),
    val filteredRows: List<ResultRow> = emptyList(),
    val upsertValues: List<String> = emptyList()
)