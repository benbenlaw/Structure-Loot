package com.benbenlaw.structureloot.block.entity.renderer;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.entity.StructureLootBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StructureLootBlockEntityRenderer implements BlockEntityRenderer<StructureLootBlockEntity, StructureLootBlockEntityRenderState> {

    private static final Identifier TEXTURE = StructureLoot.identifier("textures/block/structure_loot.png");

    private final ModelPart model;

    public StructureLootBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(StructureLootModelLayers.STRUCTURE_LOOT_MAIN);
    }

    @Override
    public StructureLootBlockEntityRenderState createRenderState() {
        return new StructureLootBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(StructureLootBlockEntity blockEntity, StructureLootBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
    }

    @Override
    public void submit(StructureLootBlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();

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

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(StructureLootBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(32.0);
    }
}