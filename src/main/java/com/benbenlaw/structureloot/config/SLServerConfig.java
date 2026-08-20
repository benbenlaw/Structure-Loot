package com.benbenlaw.structureloot.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SLServerConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> structureTokensSpawnInStructures;
    public static final ModConfigSpec.ConfigValue<Boolean> blockTokensDropFromBlocks;
    public static final ModConfigSpec.ConfigValue<Boolean> entityTokensDropFromEntities;

    static {
        BUILDER.comment("BBL Loot Generator");

        BUILDER.push("Tokens");

        structureTokensSpawnInStructures = BUILDER.comment("If true, structure tokens will spawn in structures.").define("structureTokensSpawnInStructures", true);
        blockTokensDropFromBlocks = BUILDER.comment("If true, block tokens will drop from blocks.").define("blockTokensDropFromBlocks", true);
        entityTokensDropFromEntities = BUILDER.comment("If true, entity tokens will drop from entities.").define("entityTokensDropFromEntities", true);

        BUILDER.pop();

        //End
        SPEC = BUILDER.build();
    }
}