package api.migration.models.mapo.problems.utils

import org.moeaframework.core.variable.BinaryVariable

class BooleanVariable: BinaryVariable {
    constructor(): super(1)

    constructor(binaryVariable: BinaryVariable): super(binaryVariable.numberOfBits) {
        assert(binaryVariable.numberOfBits == 1)
        value = binaryVariable.get(0)
    }

    var value: Boolean
    get() = get(0)
    set(value) {
        set(0, value)
    }
}