package api.migration.models.mapo.normalizers

class MinMaxNormalizer : Normalizer {
    override fun normalize(values: List<Double>): List<Double> {
        val min = values.minByOrNull { it }
        val max = values.maxByOrNull { it }
        return values.map { (it - min!!)/(max!! - min) }
    }
}