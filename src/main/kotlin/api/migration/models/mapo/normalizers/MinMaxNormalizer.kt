package api.migration.models.mapo.normalizers

class MinMaxNormalizer : Normalizer {
    override fun normalize(values: List<Double>): List<Double> {
        val min = values.minByOrNull { it }
        val max = values.maxByOrNull { it }
        if (min == max) {
            return List(values.size) { 0.0 }
        }
        return values.map { (it - min!!)/(max!! - min) }
    }
}