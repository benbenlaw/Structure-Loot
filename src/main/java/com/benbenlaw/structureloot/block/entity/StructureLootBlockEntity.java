package com.benbenlaw.structureloot.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.benbenlaw.structureloot.screen.custom.StructureLootMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class StructureLootBlockEntity extends SyncableBlockEntity implements MenuProvider {

    private final ContainerData data;
    private int maxProgress = 200;
    private int progress = 0;
    private boolean breakingStructure = false;

    public static final int STRUCTURE_SETTER = 0;
    public static final int UPGRADE = 1;
    public static final int FIRST_OUTPUT_SLOT = 2;
    public static final int LAST_OUTPUT_SLOT = 101;

    private final SyncableItemHandler inventory = new SyncableItemHandler(this, 102, (slot, stack) -> true, i -> i >= 2);

    public StructureLootBlockEntity(BlockPos pos, BlockState state) {
        super(SLBlockEntities.STRUCTURE_LOOT_BLOCK_ENTITY.get(), pos, state);

        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                }
            }

            public int getCount() {
                return 2;
            }
        };
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        RecipeHolder<StructureLootRecipe> recipeHolder = getRecipe();
        boolean changed = false;

        if (recipeHolder != null) {
            this.progress++;
            changed = true;

            if (this.progress >= this.maxProgress) {
                executeLootRoll(recipeHolder.value());
                this.progress = 0;
            }
        } else if (this.progress > 0) {
            this.progress = 0;
            changed = true;
        }

        if (changed) {
            setChanged();
            sync();
        }
    }

    public void breakStructure() {
        if (level == null || level.isClientSide() || breakingStructure) return;
        breakingStructure = true;

        BlockPos controllerPos = getBlockPos();

        for (BlockPos partPos : BlockPos.betweenClosed(
                controllerPos.offset(-1, -1, -1), controllerPos.offset(1, 1, 1))) {
            if (partPos.equals(controllerPos)) continue;
            if (level.getBlockState(partPos).is(SLBlocks.STRUCTURE_LOOT_PART.get())) {
                level.setBlock(partPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }

        if (level.getBlockState(controllerPos).is(SLBlocks.STRUCTURE_LOOT_BLOCK.get())) {
            level.destroyBlock(controllerPos, true);
        }
    }

    private RecipeHolder<StructureLootRecipe> getRecipe() {
        if (level == null || level.getServer() == null) return null;

        ItemStack structureStack = ItemUtil.getStack(inventory, STRUCTURE_SETTER);
        if (structureStack.isEmpty()) return null;

        Identifier structureId = structureStack.get(SLDataComponents.STRUCTURE_ID.get());
        if (structureId == null) return null;

        return level.getServer().getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(holder -> holder.value().getType() == StructureLootRecipe.TYPE)
                .map(holder -> (RecipeHolder<StructureLootRecipe>) holder)
                .filter(holder -> holder.value().structure().equals(structureId))
                .findFirst()
                .orElse(null);
    }

    private void executeLootRoll(StructureLootRecipe recipe) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<ItemStack> allDrops = new ArrayList<>();
        ReloadableServerRegistries.Holder lootRegistries = serverLevel.getServer().reloadableRegistries();
        List<Identifier> tablesToRoll = new ArrayList<>(recipe.lootTables());

        if (recipe.rolls() >= 0 && recipe.rolls() < tablesToRoll.size()) {
            Collections.shuffle(tablesToRoll, new Random(serverLevel.getSeed()));
            tablesToRoll = tablesToRoll.subList(0, recipe.rolls());
        }

        for (Identifier lootTableId : tablesToRoll) {
            ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableId);

            LootTable lootTable = lootRegistries.getLootTable(lootTableKey);
            if (lootTable == LootTable.EMPTY) continue;

            LootParams params = new LootParams.Builder(serverLevel)
                    .create(LootContextParamSets.EMPTY);

            allDrops.addAll(lootTable.getRandomItems(params, serverLevel.getRandom()));
        }
        try (Transaction tx = Transaction.open(null)) {
            inventory.runInternal(() -> {
                for (ItemStack stack : allDrops) {
                    if (stack.isEmpty()) continue;

                    int remaining = stack.getCount();
                    for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT && remaining > 0; slot++) {
                        remaining -= inventory.insert(slot, ItemResource.of(stack), remaining, tx);
                    }
                }
            });
            tx.commit();
        }

        sync();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        inventory.serialize(output.child("inventory"));
        output.putInt("progress", progress);
        output.putInt("maxProgress", maxProgress);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        inventory.deserialize(input.childOrEmpty("inventory"));
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("maxProgress", 200);
        super.loadAdditional(input);
    }

    public ItemStacksResourceHandler getItemHandler() {
        return inventory;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int container, Inventory inventory, Player player) {
        return new StructureLootMenu(container, inventory, this.worldPosition, data);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.structureloot.structure_loot");
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        dropInventoryContents(inventory);
    }
}