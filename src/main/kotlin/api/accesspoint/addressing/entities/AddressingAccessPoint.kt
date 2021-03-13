package api.accesspoint.addressing.entities

import api.accesspoint.original.entities.AccessPoint
import api.addressing.dynamic.consumer.entities.DynamicAddressingNotificationConsumerDevice
import api.addressing.fixed.entities.AddressingDevice

interface AddressingAccessPoint: DynamicAddressingNotificationConsumerDevice, AccessPoint