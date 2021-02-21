package api.common.utils

import org.fog.entities.Tuple

data class TupleRecipientPair(
    var tuple: Tuple,
    var recipientId: Int
)