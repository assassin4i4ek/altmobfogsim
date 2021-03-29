package api.migration.original.entites.addons

import api.migration.original.entites.ModuleLaunchingDevice
import api.network.dynamic.entites.DynamicGatewayConnectionDevice

interface DynamicGatewayConnectionModuleLaunchingDevice: DynamicGatewayConnectionDevice, ModuleLaunchingDevice {
    override val mParentId: Int
        get() = super.mParentId
}