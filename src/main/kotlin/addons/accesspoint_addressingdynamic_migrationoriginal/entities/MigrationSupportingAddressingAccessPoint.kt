package addons.accesspoint_addressingdynamic_migrationoriginal.entities

import addons.accesspoint_addressingdynamic.entities.AddressingAccessPoint
import api.accesspoint.migration.entities.MigrationSupportingAccessPoint

interface MigrationSupportingAddressingAccessPoint: MigrationSupportingAccessPoint, AddressingAccessPoint {
}