package turniplabs.industry.blocks.machines

import net.minecraft.core.block.BlockTileEntity
import net.minecraft.core.block.entity.TileEntity
import net.minecraft.core.block.material.Material
import net.minecraft.core.entity.player.EntityPlayer
import net.minecraft.core.world.World
import sunsetsatellite.energyapi.interfaces.mixins.IEntityPlayer
import turniplabs.industry.blocks.entities.TileEntitySolarLV

class BlockSolarArrayLV(key: String?, id: Int, material: Material?) : BlockTileEntity(key, id, material) {

    override fun getNewBlockEntity(): TileEntity {
        return TileEntitySolarLV()
    }

    override fun blockActivated(world: World?, x: Int, y: Int, z: Int, player: EntityPlayer?): Boolean {
        if (!world?.isClientSide!!) {
            val tileEntity: TileEntitySolarLV = world.getBlockTileEntity(x, y, z) as TileEntitySolarLV

            tileEntity ?: return false
            (player as IEntityPlayer).displayGuiScreen_energyapi(tileEntity)
        }
        return true
    }
}