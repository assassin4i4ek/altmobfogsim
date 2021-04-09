package api.common.utils

import org.fog.utils.distribution.DeterministicDistribution

class DeterministicDistributionWithRandomOffset(value: Double): DeterministicDistribution(value) {
    var isFirst: Boolean = true
    override fun getNextValue(): Double {
        return if (isFirst) {
            isFirst = false
            Math.random() * value
        }
        else {
            super.getNextValue()
        }
    }
}