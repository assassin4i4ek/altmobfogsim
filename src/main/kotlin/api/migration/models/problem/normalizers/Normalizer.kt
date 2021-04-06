package api.migration.models.problem.normalizers

interface Normalizer {
    fun normalize(values: List<Double>): List<Double>
}