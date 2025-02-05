package baboon.industry.block.machines.basic.entity;

import baboon.industry.IndustryConfig;
import baboon.industry.block.IndustryBlocks;
import baboon.industry.block.entity.TileEntityEnergyConductorDamageable;
import baboon.industry.block.machines.basic.BlockMachineTrommel;
import baboon.industry.item.IndustryItems;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import net.minecraft.core.WeightedRandomBag;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockChest;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.monster.EntitySlime;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.IInventory;
import net.minecraft.core.util.helper.Sides;
import sunsetsatellite.energyapi.impl.ItemEnergyContainer;
import sunsetsatellite.sunsetutils.util.Connection;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.IItemIO;

import java.util.Random;

public class TileEntityMachineTrommel extends TileEntityEnergyConductorDamageable implements IInventory, IItemIO {
    private ItemStack[] contents;
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsDirt = new WeightedRandomBag<>();
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsGravel = new WeightedRandomBag<>();
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsClay = new WeightedRandomBag<>();
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsSand = new WeightedRandomBag<>();
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsRichDirt = new WeightedRandomBag<>();
    private static final WeightedRandomBag<WeightedRandomLootObject> trommelDropsSoulSand = new WeightedRandomBag<>();
    private final Random rand = new Random();
    private int nextToSieve = 2;
    private boolean hasEnergyUpgrade;
    private int currentSpeed = 0;
    private int currentEnergy = 0;
    private int currentTransformers = 0;
    private int currentPuller = 0;
    private int currentPusher = 0;
    public boolean active = false;
    public int currentMachineTime = 0;
    public int maxMachineTime = 40;

    public TileEntityMachineTrommel() {
        contents = new ItemStack[18];

        setCapacity(IndustryConfig.cfg.getInt("Energy Values.lvMachineStorage"));
        setTransfer(IndustryConfig.cfg.getInt("Energy Values.lvIO"));
        setMaxReceive(IndustryConfig.cfg.getInt("Energy Values.lvIO"));

        for (Direction dir : Direction.values())
            setConnection(dir, Connection.INPUT);

        setTrommelDrops();
    }

    @Override
    public int getSizeInventory() {
        return contents.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return contents[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (contents[i] != null) {
            if (contents[i].stackSize <= j) {
                ItemStack itemstack = contents[i];
                contents[i] = null;
                return itemstack;
            } else {
                ItemStack splitStack = contents[i].splitStack(j);
                if (contents[i].stackSize <= 0) {
                    contents[i] = null;
                }

                return splitStack;
            }
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack) {
        contents[i] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit())
            itemStack.stackSize = getInventoryStackLimit();

        onInventoryChanged();
    }

    @Override
    public String getInvName() {
        return "IndustryMachineTrommel";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
            return false;

        return entityPlayer.distanceToSqr(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f) <= 64;
    }

    @Override
    public int getActiveItemSlotForSide(Direction direction) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int index = Sides.orientationLookUpHorizontal[6 * meta + direction.getSide()];
        direction = Direction.getDirectionFromSide(index);

        if (direction == Direction.Y_POS || direction == Direction.Y_NEG) {
            for (int inputSlots = 2; inputSlots < 6; inputSlots++) {
                if (contents[inputSlots] == null || contents[inputSlots].stackSize < 64)
                    return inputSlots;
            }
            return 2;
        } else {
            for (int outputSlots = 6; outputSlots < 14; outputSlots++) {
                if (contents[outputSlots] != null)
                    return outputSlots;
            }
            return 6;
        }
    }

    @Override
    public Connection getItemIOForSide(Direction direction) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int index = Sides.orientationLookUpHorizontal[6 * meta + direction.getSide()];
        direction = Direction.getDirectionFromSide(index);

        if (direction == Direction.Y_POS || direction == Direction.Y_NEG)
            return Connection.INPUT;

        return Connection.OUTPUT;
    }

    private static void setTrommelDrops() {
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.ammoPebble), 1, 3), 50.0);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.clay), 1, 5), 20.0);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.flint), 1, 3), 10.0);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.sulphur)), 2.0);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawIron), 1), 0.5);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawCopper), 1), 0.5);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawTin), 1), 0.5);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.olivine)), 0.25);
        trommelDropsDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.quartz)), 0.25);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.flint), 1, 2), 25.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.olivine), 1, 3), 30.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.ammoPebble), 1, 6), 25.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawIron), 1, 2), 10.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawTin), 1, 2), 10.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawCopper), 1, 2), 10.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.dye, 1, 4), 2, 6), 5.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.sulphur)), 5.0);
        trommelDropsGravel.addEntry(new WeightedRandomLootObject(new ItemStack(Item.quartz)), 0.5);
        trommelDropsClay.addEntry(new WeightedRandomLootObject(new ItemStack(Item.clay), 4, 8), 30.0);
        trommelDropsClay.addEntry(new WeightedRandomLootObject(new ItemStack(Item.ammoPebble), 1, 3), 20.0);
        trommelDropsClay.addEntry(new WeightedRandomLootObject(new ItemStack(Item.sulphur)), 10.0);
        trommelDropsClay.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawGold), 1), 1.0);
        trommelDropsClay.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawUranium), 1), 1.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.quartz), 1, 3), 50.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.clay), 4, 8), 30.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.ammoPebble), 1, 5), 25.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.bone), 1, 3), 10.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.flint), 1, 3), 10.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.sulphur)), 5.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawGold), 1), 1.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawUranium), 1), 1.0);
        trommelDropsSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.olivine)), 5.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawGold), 1), 10.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawIron), 1, 2), 15.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawTin), 1, 2), 15.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawCopper), 1, 2), 15.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.quartz), 1, 4), 25.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.olivine), 1, 4), 25.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.dye, 1, 4), 2, 4), 20.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.clay), 4, 8), 10.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(Item.ammoPebble), 1, 5), 5.0);
        trommelDropsRichDirt.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawUranium), 1), 1.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.flint), 1, 3), 20.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.bone), 1, 6), 10.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawGold), 1), 2.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.oreRawIron), 1), 1.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawTin), 1), 1.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawCopper), 1), 1.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(IndustryItems.oreRawUranium), 1), 2.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.dustGlowstone), 1, 6), 5.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.quartz), 1, 3), 5.0);
        trommelDropsSoulSand.addEntry(new WeightedRandomLootObject(new ItemStack(Item.nethercoal)), 0.5);
    }

    private boolean canItemBeTrommeled(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else {
            int itemID = itemstack.getItem().id;
            if (itemID == Block.dirt.id || itemID == Block.grass.id || itemID == Block.grassRetro.id || itemID == Block.pathDirt.id || itemID == Block.farmlandDirt.id) {
                return true;
            } else if (itemID == Block.sand.id)
                return true;
            else if (itemID == Block.dirtScorchedRich.id)
                return true;
            else if (itemID == Block.dirtScorched.id)
                return true;
            else if (itemID == Block.soulsand.id)
                return true;
            else {
                return itemID == Block.gravel.id;
            }
        }
    }

    private boolean outputNotFull() {
        return (contents[6] == null || contents[7] == null ||
                contents[8] == null || contents[9] == null ||
                contents[10] == null || contents[11] == null ||
                contents[12] == null || contents[13] == null);
    }

    private boolean canProduce(int slotIndex) {
        return contents[slotIndex] != null && this.canItemBeTrommeled(contents[slotIndex]) && outputNotFull();
    }

    private ItemStack getItemResult(ItemStack slotItem) {
        if (this.rand.nextInt(2) != 0) {
            return null;
        } else {
            int slotItemID = slotItem.getItem().id;
            if (slotItemID == Block.dirt.id
                    || slotItemID == Block.dirtScorched.id
                    || slotItemID == Block.grass.id
                    || slotItemID == Block.grassRetro.id
                    || slotItemID == Block.pathDirt.id
                    || slotItemID == Block.farmlandDirt.id) {
                return trommelDropsDirt.getRandom().getItemStack();
            } else if (slotItemID == Block.sand.id) {
                return trommelDropsSand.getRandom().getItemStack();
            } else if (slotItemID == Block.gravel.id) {
                return trommelDropsGravel.getRandom().getItemStack();
            } else if (slotItemID == Block.blockClay.id) {
                return trommelDropsClay.getRandom().getItemStack();
            } else if (slotItemID == Block.dirtScorchedRich.id) {
                return trommelDropsRichDirt.getRandom().getItemStack();
            } else {
                return slotItemID == Block.soulsand.id ? trommelDropsSoulSand.getRandom().getItemStack() : null;
            }
        }
    }

    public void sieveItem(int slotIndex) {
        if (this.canProduce(slotIndex)) {
            ItemStack itemResult = this.getItemResult(contents[slotIndex]);
            --contents[slotIndex].stackSize;
            if (contents[slotIndex].stackSize <= 0) {
                contents[slotIndex] = null;
            }

            if (itemResult != null) {
                int xOffset = 0;
                int zOffset = 0;
                int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) & 7;
                if (meta == 2) {
                    xOffset = -1;
                } else if (meta == 5) {
                    zOffset = -1;
                } else if (meta == 3) {
                    xOffset = 1;
                } else if (meta == 4) {
                    zOffset = 1;
                }

                int adjacentId = this.worldObj.getBlockId(this.xCoord + xOffset, this.yCoord, this.zCoord + zOffset);
                IInventory chest = null;
                if (Block.blocksList[adjacentId] instanceof BlockChest) {
                    chest = BlockChest.getInventory(this.worldObj, this.xCoord + xOffset, this.yCoord, this.zCoord + zOffset);
                }

                if (chest != null) {
                    for(int i = 0; i < chest.getSizeInventory(); ++i) {
                        ItemStack slot = chest.getStackInSlot(i);
                        if (slot != null && slot.itemID == itemResult.itemID && slot.getMetadata() == itemResult.getMetadata()) {
                            while(slot.stackSize + 1 <= slot.getMaxStackSize()) {
                                ++slot.stackSize;
                                chest.setInventorySlotContents(i, slot);
                                if (itemResult.stackSize <= 0) {
                                    return;
                                }

                                --itemResult.stackSize;
                            }
                        }
                    }

                    if (itemResult.stackSize <= 0) {
                        return;
                    }

                    for(int i = 0; i < chest.getSizeInventory(); ++i) {
                        ItemStack slot = chest.getStackInSlot(i);
                        if (slot == null) {
                            chest.setInventorySlotContents(i, itemResult);
                            return;
                        }
                    }
                }

                int randOutput = rand.nextInt(14 - 6) + 6;
                if (itemResult.stackSize > 0) {
                    if (contents[randOutput] == null)
                        contents[randOutput] = new ItemStack(itemResult);
                    else
                        if (contents[randOutput].getItem() == itemResult.getItem())
                            ++contents[randOutput].stackSize;
                }
            }

            if (this.rand.nextInt(4000) == 0) {
                float f = 0.125F;
                float f1 = 0.125F;
                EntitySlime entityslime = new EntitySlime(this.worldObj);
                entityslime.setSlimeSize(1);
                entityslime.moveTo(
                        (double)this.xCoord + (double)f, (double)this.yCoord + 1.0, (double)this.zCoord + (double)f1, this.rand.nextFloat() * 360.0F, 0.0F
                );
                float f3 = 0.05F;
                entityslime.xd = (float)this.rand.nextGaussian() * f3;
                entityslime.yd = (float)this.rand.nextGaussian() * f3 + 0.2F;
                entityslime.zd = (float)this.rand.nextGaussian() * f3;
                this.worldObj.entityJoinedWorld(entityslime);
            }
        }
    }

    @Override
    public void onInventoryChanged() {
        currentSpeed = 0;
        currentEnergy = 0;
        currentTransformers = 0;
        maxMachineTime = 40;
        currentPuller = 0;
        currentPusher = 0;
        capacity = IndustryConfig.cfg.getInt("Energy Values.lvMachineStorage");

        for (int upgradesSize = 4; upgradesSize < contents.length; upgradesSize++) {
            if (contents[upgradesSize] != null) {
                if (contents[upgradesSize].getItem() == IndustryItems.upgradeSpeed) {
                    currentSpeed += 1;
                    maxMachineTime *= 1 - 0.3;
                }

                if (contents[upgradesSize].getItem() == IndustryItems.upgradeEnergy) {
                    currentEnergy += 1;
                    capacity += 10000;
                }

                if (contents[upgradesSize].getItem() == IndustryItems.upgradeTransformer)
                    currentTransformers += 1;

                if (contents[upgradesSize].getItem() == IndustryItems.upgradePuller)
                    currentPuller = 1;

                if (contents[upgradesSize].getItem() == IndustryItems.upgradePusher)
                    currentPusher = 1;
            }
        }
        super.onInventoryChanged();
    }

    private void pullFromTop() {
        TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        if (tile instanceof IInventory) {
            for (int tileInv = 0; tileInv < ((IInventory) tile).getSizeInventory(); tileInv++) {
                ItemStack tileStack = ((IInventory) tile).getStackInSlot(tileInv);

                if (tileStack != null) {
                    if (tileStack.stackSize - 4 > 0) {

                        for (int inputSlots = 2; inputSlots < 6; inputSlots++) {
                            if (contents[inputSlots] == null) {
                                contents[inputSlots] = new ItemStack(tileStack.getItem().id, 1, tileStack.getMetadata());
                                --tileStack.stackSize;
                            }
                            if (contents[inputSlots] != null && contents[inputSlots].stackSize + 2 <= contents[inputSlots].getMaxStackSize())
                                if (contents[inputSlots] != null && contents[inputSlots].itemID == tileStack.itemID && contents[inputSlots].getMetadata() == tileStack.getMetadata()) {
                                    ++contents[inputSlots].stackSize;
                                    --tileStack.stackSize;
                                }
                        }
                    }
                }
            }
        }
    }

    private void pushToSide() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        TileEntity tile;

        switch (meta) {
            default:
            case 2:
                tile = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
                break;
            case 3:
                tile = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
                break;
            case 4:
                tile = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
                break;
            case 5:
                tile = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
                break;
        }

        if (tile instanceof IInventory) {
            for (int tileInv = 0; tileInv < ((IInventory) tile).getSizeInventory(); tileInv++) {
                ItemStack tileStack = ((IInventory) tile).getStackInSlot(tileInv);

                for (int outputSlots = 6; outputSlots < 14; outputSlots++) {
                    if (contents[outputSlots] != null) {
                        if (tileStack == null) {
                            ((IInventory) tile).setInventorySlotContents(tileInv, new ItemStack(contents[outputSlots].getItem(), 1, contents[outputSlots].getMetadata()));
                            --contents[outputSlots].stackSize;
                        }

                        if (tileStack != null && tileStack.stackSize + 1 <= tileStack.getMaxStackSize())
                            if (tileStack.itemID == contents[outputSlots].itemID && tileStack.getMetadata() == contents[outputSlots].getMetadata()) {
                                ++tileStack.stackSize;
                                --contents[outputSlots].stackSize;
                            }

                        if (contents[outputSlots].stackSize <= 0)
                            contents[outputSlots] = null;
                    }
                }
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        boolean hasEnergy = energy > 0;

        if (!worldObj.isClientSide) {
            if (getStackInSlot(0) != null && getStackInSlot(0).getItem() instanceof ItemEnergyContainer) {
                provide(getStackInSlot(0), getMaxProvide(), false);
                onInventoryChanged();
            }

            if (getStackInSlot(1) != null && getStackInSlot(1).getItem() instanceof ItemEnergyContainer) {
                receive(getStackInSlot(1), getMaxReceive(), false);
                onInventoryChanged();
            }

            if (worldObj.getBlockId(xCoord, yCoord, zCoord) == IndustryBlocks.machineTrommel.id &&
                    currentMachineTime == 0 &&
                    contents[nextToSieve] == null) {
                BlockMachineTrommel.updateBlockState(true, worldObj, xCoord, yCoord, zCoord);
                onInventoryChanged();
            }

            if (!this.canProduce(nextToSieve))
                nextSieveId();

            if (nextToSieve > 5)
                nextToSieve = 2;

            if (energy > capacity)
                energy = 0;

            switch (currentTransformers) {
                case 1:
                    setMaxReceive(IndustryConfig.cfg.getInt("Energy Values.mvIO"));
                    break;
                case 2:
                    setMaxReceive(IndustryConfig.cfg.getInt("Energy Values.hvIO"));
                    break;
                case 3:
                case 4:
                    setMaxReceive(IndustryConfig.cfg.getInt("Energy Values.ehvIO"));
                    break;
            }

            if (currentPuller > 0)
                pullFromTop();

            if (currentPusher > 0)
                pushToSide();

            if (currentMachineTime > maxMachineTime)
                currentMachineTime = 0;

            if (hasEnergy && canProduce(nextToSieve)) {
                ++currentMachineTime;
                energy -= 3;
                active = true;

                if (currentSpeed > 0 && energy - 20 * currentSpeed >= -20)
                    energy -= 20 * currentSpeed;

                if (currentMachineTime >= maxMachineTime) {
                    currentMachineTime = 0;
                    sieveItem(nextToSieve);
                    active = false;
                    onInventoryChanged();
                }
            } else {
                currentMachineTime = 0;
                active = false;
            }

            if (active)
                worldObj.notifyBlockChange(xCoord, yCoord, zCoord, IndustryBlocks.machineTrommel.id);
        }
    }
    private void nextSieveId(){
        nextToSieve++;
        if (nextToSieve > 5){
            nextToSieve = 2;
        }
        if (nextToSieve < 2){
            nextToSieve = 2;
        }
    }
    public float getTrommelProgressPercent(int i) {
        return (float)this.currentMachineTime / (float)this.maxMachineTime * (float)i;
    }

    @Override
    public void writeToNBT(CompoundTag CompoundTag) {
        super.writeToNBT(CompoundTag);
        CompoundTag.putInt("Health", machineHealth);
        CompoundTag.putInt("MachineTime", currentMachineTime);
        CompoundTag.putInt("Energy", energy);

        ListTag listTag = new ListTag();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                CompoundTag compoundTag2 = new CompoundTag();

                compoundTag2.putInt("Slot", i);
                contents[i].writeToNBT(compoundTag2);
                listTag.addTag(compoundTag2);
            }
        }
        CompoundTag.put("Items", listTag);
    }

    @Override
    public void readFromNBT(CompoundTag CompoundTag) {
        super.readFromNBT(CompoundTag);
        this.machineHealth = CompoundTag.getInteger("Health");
        this.currentMachineTime = CompoundTag.getInteger("MachineTime");
        this.energy = CompoundTag.getInteger("Energy");

        ListTag listTag = CompoundTag.getList("Items");

        contents = new ItemStack[getSizeInventory()];
        for (int i = 0; i < listTag.tagCount(); i++) {
            CompoundTag compoundTag2 = (com.mojang.nbt.CompoundTag) listTag.tagAt(i);
            int slot = compoundTag2.getInteger("Slot");

            if (slot >= 0 && slot < contents.length)
                contents[slot] = ItemStack.readItemStackFromNbt(compoundTag2);
        }
    }
}
