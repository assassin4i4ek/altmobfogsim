package api.common.utils

import org.fog.entities.Tuple

data class TupleNextHopTargetsContainer(
        val tuple: Tuple,
        val nextHopId: Int,
        val targetIds: List<Int>
)
