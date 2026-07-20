package com.benbenlaw.structureloot.screen.custom;

import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ScrollableResultSlot extends Slot {

    private final StructureLootMenu menu;
    private final int visibleIndex;

    public ScrollableResultSlot(StructureLootMenu menu, int visibleIndex, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        this.menu = menu;
        this.visibleIndex = visibleIndex;
    }

    private int realIndex() {
        return 2 + visibleIndex + (menu.getScrollOffset() * StructureLootMenu.COLUMNS);
    }

    private SyncableItemHandler handler() {
        return (SyncableItemHandler) menu.blockEntity.getItemHandler();
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public ItemStack getItem() {
        int real = realIndex();
        var h = handler();
        if (real < 0 || real >= h.size()) return ItemStack.EMPTY;
        ItemStack stack = ItemUtil.getStack(h, real);
        container.setItem(0, stack);
        return stack;
    }

    @Override
    public void set(ItemStack stack) {
        int real = realIndex();
        var h = handler();
        if (real < 0 || real >= h.size()) return;

        h.runInternal(() -> {
            try (Transaction tx = Transaction.openRoot()) {
                if (stack.isEmpty()) {
                    h.set(real, ItemResource.EMPTY, 0);
                } else {
                    h.set(real, ItemResource.of(stack), stack.getCount());
                }
                tx.commit();
            }
        });
        setChanged();
    }
    @Override
    public ItemStack remove(int amount) {
        int real = realIndex();
        var h = handler();
        
        if (real < 0 || real >= h.size()) return ItemStack.EMPTY;
        
        ItemStack current = ItemUtil.getStack(h, real);
        if (current.isEmpty()) return ItemStack.EMPTY;

        int take = Math.min(amount, current.getCount());
        ItemStack result = current.copy();
        result.setCount(take);

        h.runInternal(() -> {
            try (Transaction tx = Transaction.openRoot()) {
                ItemStack remaining = current.copy();
                remaining.shrink(take);
                if (remaining.isEmpty()) {
                    h.set(real, ItemResource.EMPTY, 0);
                } else {
                    h.set(real, ItemResource.of(remaining), remaining.getCount());
                }
                tx.commit();
            }
        });

        setChanged();
        return result;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // output only
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}