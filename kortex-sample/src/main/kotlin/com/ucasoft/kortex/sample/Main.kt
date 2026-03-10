package com.ucasoft.kortex.sample

import com.ucasoft.kortex.sample.output.StatusRow
import com.ucasoft.kortex.sample.output.StatusTablePrinter
import com.ucasoft.kortex.startKortexObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext

suspend fun main() {
    startKortexObservable { entities ->
        val lights = entities.lights
        val scope = CoroutineScope(currentCoroutineContext())
        val rows = lights.map { light ->
            StatusRow(
                entityId = light.entityId,
                friendlyName = light.attributes.friendlyName.ifBlank { light.entityId },
                status = light.state
            )
        }
        val tablePrinter = StatusTablePrinter(rows)

        tablePrinter.render()

        lights.forEach { light ->
            light.onToggled(scope) {
                tablePrinter.updateStatus(entityId, state)
            }
        }
    }
}