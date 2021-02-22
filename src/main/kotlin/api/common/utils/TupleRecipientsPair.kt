package api.common.utils

import org.fog.entities.Tuple

data class TupleRecipientsPair(
        val tuple: Tuple,
        val notifierIds: List<Int>
)