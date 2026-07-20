package com.benbenlaw.structureloot.block.entity.renderer;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.entity.StructureLootBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState.ChestMaterialType;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StructureLootBlockEntityRenderer implements BlockEntityRenderer<StructureLootBlockEntity, StructureLootBlockEntityRenderState> {

    private static final Identifier TEXTURE = StructureLoot.identifier("textures/block/structure_loot.png");
    private static final float SPIN_SPEED = 2.0F;

    private final ModelPart model;
    private final ChestModel chestModel;
    private final SpriteGetter sprites;
    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public StructureLootBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(StructureLootModelLayers.STRUCTURE_LOOT_MAIN);
        this.chestModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.sprites = context.sprites();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public StructureLootBlockEntityRenderState createRenderState() {
        return new StructureLootBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(StructureLootBlockEntity blockEntity, StructureLootBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);

        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
        renderState.spinAngle = (gameTime + partialTick) * SPIN_SPEED % 360.0F;

        int progress = blockEntity.getProgress();
        int maxProgress = blockEntity.getMaxProgress();

        if (progress <= 0) {
            renderState.chestOpenness = 0.0F;
        } else {
            float openRampEnd = maxProgress * 0.15F;
            float closeRampStart = maxProgress * 0.85F;

            if (progress < openRampEnd) {
                renderState.chestOpenness = progress / openRampEnd;
            } else if (progress < closeRampStart) {
                renderState.chestOpenness = 1.0F;
            } else {
                renderState.chestOpenness = 1.0F - (progress - closeRampStart) / (maxProgress - closeRampStart);
            }
        }
    }

    @Override
    public void submit(StructureLootBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0, 0.0, 0.0);

            submitNodeCollector.submitModelPart(
                    model,
                    poseStack,
                    RenderTypes.entityTranslucent(TEXTURE),
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    null
            );
        } finally {
            poseStack.popPose();
        }

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(renderState.spinAngle));
            poseStack.translate(-0.5, -0.5, -0.5);

            SpriteId spriteId = Sheets.chooseSprite(ChestMaterialType.REGULAR, ChestType.SINGLE);

            submitNodeCollector.submitModel(
                    chestModel,
                    renderState.chestOpenness,
                    poseStack,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    spriteId,
                    sprites,
                    0,
                    renderState.breakProgress
            );
        } finally {
            poseStack.popPose();
        }

        if (renderState.showFallingItem && renderState.previewItem != null && !renderState.previewItem.isEmpty()) {
            poseStack.pushPose();
            try {
                double startY = 1.3; // above the open lid
                double endY = 0.6;   // resting inside the chest
                double currentY = startY + (endY - startY) * renderState.itemFallProgress;

                poseStack.translate(0.5, currentY, 0.5);
                poseStack.scale(0.5F, 0.5F, 0.5F);

                itemModelResolver.updateForTopItem(
                        itemRenderState,
                        renderState.previewItem,
                        ItemDisplayContext.GROUND,
                        null,
                        null,
                        (int) renderState.spinAngle // reused as a cheap seed - fine since ground items don't need real randomness here
                );

                itemRenderState.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            } finally {
                poseStack.popPose();
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(StructureLootBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(32.0);
    }
}