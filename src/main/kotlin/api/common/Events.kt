package api.common

enum class Events(
    val tag: Int
) {
    NETWORK_DEVICE_ADDRESS_TUPLE(6000),

    DYNAMIC_GATEWAY_CONNECTION_CHANGED(6001),

    MOBILE_DEVICE_NEXT_MOVE(6002),
}