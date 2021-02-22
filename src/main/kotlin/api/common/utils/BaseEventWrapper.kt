package api.common.utils

import org.cloudbus.cloudsim.core.SimEvent

data class BaseEventWrapper<T>(
        val baseEvent: SimEvent,
        val other: T
)
