package api.migration.entites

import api.network.dynamic.entites.DynamicGatewayConnectionDevice

interface DynamicGatewayConnectionMobileLaunchingDevice: DynamicGatewayConnectionDevice, ModuleLaunchingDevice {
    override val mParentId: Int
        get() = super.mParentId
}