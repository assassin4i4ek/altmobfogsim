package api.migration.models.mapo.normalizers

interface Normalizer {
    fun normalize(values: List<Double>): List<Double>
}