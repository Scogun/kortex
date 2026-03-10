package com.ucasoft.kortex.sample.output

internal class StatusTablePrinter(rows: List<StatusRow>) {
    private val orderedRows = rows.sortedBy { it.friendlyName.lowercase() }
    private val rowsById = orderedRows.associateBy { it.entityId }
    private val rowIndexById = orderedRows.mapIndexed { index, row -> row.entityId to index }.toMap()

    private val nameHeader = "friendlyName"
    private val statusHeader = "status"
    private val nameWidth = maxOf(nameHeader.length, orderedRows.maxOfOrNull { it.friendlyName.length } ?: 0)
    private val statusWidth = maxOf(statusHeader.length, orderedRows.maxOfOrNull { it.status.length } ?: 0)
    private val separator = "+-${"-".repeat(nameWidth)}-+-${"-".repeat(statusWidth)}-+"
    private val tableBottomLine = orderedRows.size + 4

    @Synchronized
    fun render() {
        print("\u001B[H\u001B[2J")
        println(separator)
        println("| ${nameHeader.padEnd(nameWidth)} | ${statusHeader.padEnd(statusWidth)} |")
        println(separator)
        orderedRows.forEach { row ->
            println(renderRow(row))
        }
        println(separator)
        moveTo(tableBottomLine + 1, 1)
        System.out.flush()
    }

    @Synchronized
    fun updateStatus(entityId: String, status: String) {
        val row = rowsById[entityId] ?: return
        if (row.status == status) return
        row.status = status

        val rowIndex = rowIndexById[entityId] ?: return
        val rowLineInTable = 4 + rowIndex

        moveTo(rowLineInTable, 1)
        print(renderRow(row))
        print("\u001B[0K")
        moveTo(tableBottomLine + 1, 1)
        System.out.flush()
    }

    private fun moveTo(lineNumber: Int, columnNumber: Int) {
        print("\u001B[${lineNumber};${columnNumber}H")
    }

    private fun renderRow(row: StatusRow): String =
        "| ${row.friendlyName.padEnd(nameWidth)} | ${row.status.padEnd(statusWidth)} |"
}