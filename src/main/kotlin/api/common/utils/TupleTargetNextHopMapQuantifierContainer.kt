package api.common.utils

import api.addressing.models.AddressingModel
import org.fog.entities.Tuple

data class TupleTargetNextHopMapQuantifierContainer(
        val tuple: Tuple,
        val targetNextHopMap: Map<Int, Int>,
        val quantifier: AddressingModel.Quantifier
)
