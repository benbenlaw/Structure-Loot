package com.benbenlaw.structureloot.screen.custom;

import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.InputSlot;
import com.benbenlaw.structureloot.block.entity.StructureLootBlockEntity;
import com.benbenlaw.structureloot.screen.SLMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StructureLootMenu extends SimpleAbstractContainerMenu {

    protected StructureLootBlockEntity blockEntity;
    protected Level level;
    protected ContainerData data;
    protected Player player;
    protected BlockPos blockPos;

    static final int COLUMNS = 5;
    public static final int VISIBLE_ROWS = 3;
    public static final int VISIBLE_SLOTS = COLUMNS * VISIBLE_ROWS;

    private int scrollOffset = 0;

    public StructureLootMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), new SimpleContainerData(2));
    }

    public StructureLootMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(SLMenuTypes.STRUCTURE_LOOT_MENU.get(), containerID, inventory, blockPos, 17);

        this.player = inventory.player;
        this.blockPos = blockPos;
        this.level = inventory.player.level();
        this.blockEntity = (StructureLootBlockEntity) this.level.getBlockEntity(blockPos);
        this.data = data;

        assert blockEntity != null;


        this.addSlot(new InputSlot(blockEntity.getItemHandler(), blockEntity.getItemHandler()::set, 0, 8, 35) {

        });

        this.addSlot(new InputSlot(blockEntity.getItemHandler(), blockEntity.getItemHandler()::set, 1, 8, 53) {

        });

        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            this.addSlot(new ScrollableResultSlot(this, i, 62 + col * 18, 17 + row * 18));
        }

        addDataSlots(data);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int value) {
        this.scrollOffset = Math.clamp(value, 0, getMaxScroll());
    }

    public int getMaxScroll() {
        int total = Math.max(0, blockEntity.getItemHandler().size() - 2);
        int rows = (total + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - VISIBLE_ROWS);
    }

    public int getRealSlot(int visibleIndex) {
        return 2 + visibleIndex + (scrollOffset * COLUMNS);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int max = data.get(1);
        return max == 0 ? 0 : (progress * 24 / max);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int inputStart = 36;
        int inputEnd = 38; // slots 36 and 37 (the 2 input slots)
        int resultStart = 38;
        int resultEnd = 38 + VISIBLE_SLOTS;

        if (pIndex < 36) {
            if (!this.moveItemStackTo(sourceStack, inputStart, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < inputEnd) {
            if (!this.moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < resultEnd) {
            if (!this.moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

}