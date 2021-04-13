package api.migration.models.mapo.problems.utils

import org.moeaframework.core.PRNG
import org.moeaframework.core.variable.BinaryVariable


class SingleBitSetBinaryVariable: BinaryVariable {
    constructor(numberOfBits: Int): super(numberOfBits) {
        randomize()
    }

    constructor(binaryVariable: BinaryVariable): super(binaryVariable.numberOfBits) {
        val onesIndexes = mutableListOf<Int>()
        for (i in 0 until numberOfBits) {
            val iValue = binaryVariable.get(i)
            if (iValue) {
                onesIndexes.add(i)
            }
            set(i, iValue)
        }
        if (onesIndexes.size > 1) {
            onesIndexes.forEach { oneIndex ->
                set(oneIndex, false)
            }
            set(onesIndexes[PRNG.nextInt(onesIndexes.size)], true)
        }
        else if (onesIndexes.isEmpty()) {
            set(PRNG.nextInt(numberOfBits), true)
        }
    }

    override fun randomize() {
        val bitSetPosition = PRNG.nextInt(numberOfBits)
        for (i in 0 until numberOfBits) {
            set(i, false)
        }
        set(bitSetPosition, true)
    }
}