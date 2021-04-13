package api.common.utils

import org.fog.utils.distribution.DeterministicDistribution

class DeterministicDistributionWithOffset(private val firstOffset: Double, period: Double): DeterministicDistribution(period) {
    var isFirst: Boolean = true
    override fun getNextValue(): Double {
        return if (isFirst) {
            isFirst = false
            firstOffset
        }
        else {
            super.getNextValue()
        }
    }
}