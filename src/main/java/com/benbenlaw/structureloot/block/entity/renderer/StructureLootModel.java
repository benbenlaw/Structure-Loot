package com.benbenlaw.structureloot.block.entity.renderer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class StructureLootModel {


    //Created in Block Bench
    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -48.0F, -24.0F, 48.0F, 6.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(168, 108).addBox(20.0F, -42.0F, -24.0F, 4.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(-24.0F, -6.0F, -24.0F, 48.0F, 6.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(168, 148).addBox(20.0F, -42.0F, 20.0F, 4.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 184).addBox(-24.0F, -42.0F, 20.0F, 4.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 184).addBox(-24.0F, -42.0F, -24.0F, 4.0F, 36.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 108).addBox(-20.0F, -42.0F, 21.0F, 40.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(84, 108).addBox(-20.0F, -42.0F, -23.0F, 40.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(84, 146).addBox(-1.0F, -36.0F, -1.0F, 40.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -6.0F, -19.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 146).addBox(-1.0F, -36.0F, -1.0F, 40.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.0F, -6.0F, -19.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
}