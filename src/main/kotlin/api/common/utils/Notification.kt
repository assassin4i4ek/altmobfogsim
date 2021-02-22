package api.common.utils

data class Notification<T>(
        val data: T,
        val consumerId: Int
)
