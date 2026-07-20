package com.benbenlaw.structureloot.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class StructureLootSpecialRenderer implements SpecialModelRenderer<StructureLootSpecialRenderer.RenderState> {

    private static final Identifier TEXTURE = com.benbenlaw.structureloot.StructureLoot.identifier("textures/block/structure_loot.png");

    private final ModelPart model;

    public StructureLootSpecialRenderer(ModelPart model) {
        this.model = model;
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasReflection, int layer) {
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(-0.5, -0.5, -0.5);

            submitNodeCollector.submitModelPart(
                    model,
                    poseStack,
                    RenderTypes.entityTranslucent(TEXTURE),
                    light,
                    overlay,
                    null
            );
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public RenderState extractArgument(ItemStack itemStack) {
        return RenderState.INSTANCE;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(1, 1, 1));
    }

    public static final class RenderState {
        public static final RenderState INSTANCE = new RenderState();
        private RenderState() {}
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderState> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderState>> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<RenderState> bake(@NonNull BakingContext context) {
            ModelPart root = context.entityModelSet().bakeLayer(StructureLootModelLayers.STRUCTURE_LOOT_MAIN);
            return new StructureLootSpecialRenderer(root);
        }
    }
}