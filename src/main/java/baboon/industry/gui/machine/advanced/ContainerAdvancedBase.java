package baboon.industry.gui.machine.advanced;

import baboon.industry.block.machines.advanced.entity.TileEntityAdvancedBase;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.player.inventory.InventoryPlayer;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotFurnace;
import sunsetsatellite.energyapi.template.containers.ContainerEnergy;

import java.util.List;

public class ContainerAdvancedBase extends ContainerEnergy {

    public ContainerAdvancedBase(InventoryPlayer inventory, TileEntityAdvancedBase tileEntity) {
        tile = tileEntity;
        addSlot(new Slot(tileEntity, 0, 8, 17));
        addSlot(new Slot(tileEntity, 1, 8, 53));
        addSlot(new Slot(tileEntity, 2, 56, 25));
        addSlot(new Slot(tileEntity, 3, 56, 43));
        addSlot(new SlotFurnace(inventory.player, tileEntity, 4, 116, 25));
        addSlot(new SlotFurnace(inventory.player, tileEntity, 5, 116, 43));
        addSlot(new Slot(tileEntity, 6, 151, 53));

        for(int xSlot = 0; xSlot < 3; ++xSlot)
            for (int ySlot = 0; ySlot < 9; ++ySlot)
                addSlot(new Slot(inventory, ySlot + xSlot * 9 + 9, 8 + ySlot * 18, 84 + xSlot * 18));

        for(int hotbar = 0; hotbar < 9; ++hotbar)
            addSlot(new Slot(inventory, hotbar, 8 + hotbar * 18, 142));
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
        return ((TileEntityAdvancedBase) tile).canInteractWith(entityPlayer);
    }

    @Override
    public List<Integer> getTargetSlots(InventoryAction action, Slot slot, int target, EntityPlayer player) {
        if (slot.id >= 7 && slot.id <= 43) { // Entire inventory
            if (target == 1)    // Batteries
                return getSlots(0, 1, true);

            if (target == 2)    // Ingredients
                return getSlots(2, 2, false);

            if (target == 3)    // Redstone
                return getSlots(6, 1, false);

            if (slot.id < 34)   // Inventory > Hotbar
                return getSlots(34, 9, false);

            // Hotbar > Inventory
            return getSlots(7, 27, false);
        }
        if (slot.id < 0)
            return null;

        return getSlots(7, 36, false);
    }
}
