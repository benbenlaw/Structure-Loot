package com.benbenlaw.structureloot.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.core.util.FakePlayerUtil;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe.LootRoll;
import com.benbenlaw.structureloot.screen.custom.StructureLootMenu;
import com.benbenlaw.structureloot.util.EnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
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
    private FakePlayer fakePlayer;

    private final SyncableItemHandler inventory = new SyncableItemHandler(this, 102, (slot, stack) -> true, i -> i >= 2);
    private final EnergyHandler energyHandler = new EnergyHandler(1000000, 100000, this);

    public StructureLootBlockEntity(BlockPos pos, BlockState state) {
        super(SLBlockEntities.STRUCTURE_LOOT_BLOCK_ENTITY.get(), pos, state);

        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> energyHandler.getAmountAsInt();
                    case 3 -> energyHandler.getCapacityAsInt();
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                    case 2 -> energyHandler.getAmountAsInt();
                    case 3 -> energyHandler.getCapacityAsInt();
                }
            }

            public int getCount() {
                return 4;
            }
        };
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (fakePlayer == null) {
            fakePlayer = FakePlayerUtil.createFakePlayer((ServerLevel) level, "Structure_Loot_Block_Entity");
        }

        ItemStack structureToken = ItemUtil.getStack(inventory, STRUCTURE_SETTER);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, structureToken);

        RecipeHolder<StructureLootRecipe> recipeHolder = getRecipe();
        boolean changed = false;

        if (recipeHolder != null) {
            StructureLootRecipe recipe = recipeHolder.value();
            this.maxProgress = recipe.duration();

            if (hasOutputSpace() && consumeEnergy(recipe.rfPerTick())) {
                this.progress++;
                changed = true;

                if (level instanceof ServerLevel serverLevel && level.getGameTime() % 2 == 0) {
                    spawnLootParticle(serverLevel);
                }

                if (this.progress >= this.maxProgress) {
                    executeLootRoll(recipe, structureToken);
                    this.progress = 0;
                }
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

    private boolean consumeEnergy(int amount) {
        try (Transaction tx = Transaction.open(null)) {
            int extracted = energyHandler.extract(amount, tx);
            if (extracted == amount) {
                tx.commit();
                return true;
            }
            return false;
        }
    }

    private boolean hasOutputSpace() {
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            if (ItemUtil.getStack(inventory, slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void spawnLootParticle(ServerLevel serverLevel) {
        BlockPos pos = getBlockPos();
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.6;
        double centerZ = pos.getZ() + 0.5;

        double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2;
        double radius = 1.2 + serverLevel.getRandom().nextDouble() * 0.5;

        double spawnX = centerX + Math.cos(angle) * radius;
        double spawnY = centerY + (serverLevel.getRandom().nextDouble() - 0.5) + 0.5;
        double spawnZ = centerZ + Math.sin(angle) * radius;

        double velX = (centerX - spawnX) * 0.05;
        double velY = (centerY - spawnY) * 0.05;
        double velZ = (centerZ - spawnZ) * 0.05;

        serverLevel.sendParticles(ParticleTypes.END_ROD, spawnX, spawnY, spawnZ, 0, velX, velY, velZ, 1.0);
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

        Identifier structureId = structureStack.get(SLDataComponents.LOOT_ID.get());
        if (structureId == null) return null;

        return level.getServer().getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(holder -> holder.value().getType() == StructureLootRecipe.TYPE)
                .map(holder -> (RecipeHolder<StructureLootRecipe>) holder)
                .filter(holder -> holder.value().lootId().equals(structureId))
                .findFirst()
                .orElse(null);
    }

    private void executeLootRoll(StructureLootRecipe recipe, ItemStack structureToken) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<ItemStack> allDrops = new ArrayList<>();
        ReloadableServerRegistries.Holder lootRegistries = serverLevel.getServer().reloadableRegistries();
        List<LootRoll> tableEntries = recipe.lootTables();

        int rollCount = recipe.rolls() >= 0 ? recipe.rolls() : tableEntries.size();
        Random random = new Random(serverLevel.getSeed() + level.getGameTime());

        for (int i = 0; i < rollCount; i++) {
            LootRoll roll = tableEntries.get(random.nextInt(tableEntries.size()));

            ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, roll.table());
            LootTable lootTable = lootRegistries.getLootTable(lootTableKey);
            if (lootTable == LootTable.EMPTY) continue;

            LootParams params = buildLootParams(serverLevel, roll);
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

                if (structureToken.isDamageableItem()) {
                    structureToken.hurtAndConvertOnBreak(1, Items.AIR, fakePlayer, fakePlayer.getEquipmentSlotForItem(structureToken));
                    inventory.set(0, ItemResource.of(structureToken), structureToken.getCount());

                    if (structureToken.isEmpty()) {
                        level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }

            });
            tx.commit();
        }

        sync();
    }

    private LootParams buildLootParams(ServerLevel serverLevel, LootRoll roll) {
        Vec3 origin = Vec3.atCenterOf(getBlockPos());

        return switch (roll.type()) {
            case GENERIC -> new LootParams.Builder(serverLevel)
                    .create(LootContextParamSets.EMPTY);

            case BLOCK -> {
                BlockState fakeState = roll.blockId()
                        .map(BuiltInRegistries.BLOCK::getValue)
                        .map(net.minecraft.world.level.block.Block::defaultBlockState)
                        .orElse(Blocks.AIR.defaultBlockState());

                ItemStack tool = ItemUtil.getStack(inventory, UPGRADE);

                yield new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, origin)
                        .withParameter(LootContextParams.TOOL, tool)
                        .withParameter(LootContextParams.BLOCK_STATE, fakeState)
                        .create(LootContextParamSets.BLOCK);
            }

            case ENTITY -> {
                EntityType<?> type = roll.entityId()
                        .<EntityType<?>>map(BuiltInRegistries.ENTITY_TYPE::getValue)
                        .orElse(EntityType.PIG);

                Entity fakeEntity = type.create(serverLevel, EntitySpawnReason.EVENT);
                if (fakeEntity != null) {
                    fakeEntity.setPos(origin.x, origin.y, origin.z);
                }

                DamageSource damageSource = serverLevel.damageSources().generic();

                LootParams.Builder builder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, origin)
                        .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource);

                if (fakeEntity != null) {
                    builder.withOptionalParameter(LootContextParams.THIS_ENTITY, fakeEntity);
                }

                yield builder.create(LootContextParamSets.ENTITY);
            }
        };
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        inventory.serialize(output.child("inventory"));
        energyHandler.serialize(output.child("energy"));
        output.putInt("progress", progress);
        output.putInt("maxProgress", maxProgress);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        inventory.deserialize(input.childOrEmpty("inventory"));
        energyHandler.deserialize(input.childOrEmpty("energy"));
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("maxProgress", 200);
        super.loadAdditional(input);
    }

    public ItemStacksResourceHandler getItemHandler() {
        return inventory;
    }

    public EnergyHandler getEnergyHandler() {
        return energyHandler;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int container, Inventory inventory, Player player) {
        return new StructureLootMenu(container, inventory, this.worldPosition, data);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.structureloot.structure_loot_block");
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        dropInventoryContents(inventory);
    }
}