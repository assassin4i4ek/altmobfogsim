package api.common.utils

import org.fog.entities.Tuple

data class TupleNextHopsTargetsContainer(
        val tuple: Tuple,
        val targetNextHopMap: Map<Int, Int>
)
