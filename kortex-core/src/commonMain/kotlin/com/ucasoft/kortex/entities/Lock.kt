package com.ucasoft.kortex.entities

import com.ucasoft.kortex.client.KortexContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Lock(stateFlow: StateFlow<State>, context: KortexContext): EntityState<LockAttributes>(stateFlow, context) {

    override val attributesFlow = mapAttributes<LockAttributes>()

    val isOpenSuspend = (attributes.supportedFeatures and OPEN) != 0

    fun `is`(state: LockState) = this.state == state.name.lowercase()

    private companion object {
        const val OPEN = 1
    }
}

@Serializable
class LockAttributes: FeaturedAttributes() {

    @SerialName("changed_by")
    var changedBy: String? = null

}

enum class LockState {
    JAMMED,
    OPENING,
    LOCKING,
    OPEN,
    UNLOCKING,
    LOCKED,
    UNLOCKED
}