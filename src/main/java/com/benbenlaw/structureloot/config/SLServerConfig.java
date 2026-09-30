package com.benbenlaw.structureloot.config;

import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
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
        blockTokensDropFromBlocks = BUILDER.comment("If true, block tokens will drop from blocks.").define("blockTokensDropFromBlocks", false);
        entityTokensDropFromEntities = BUILDER.comment("If true, entity tokens will drop from entities.").define("entityTokensDropFromEntities", false);

        BUILDER.pop();

        //End
        SPEC = BUILDER.build();
    }

    public static boolean isTokenEnabled(StructureLootRecipe.LootContextType type) {
        return switch (type) {
            case GENERIC -> structureTokensSpawnInStructures.get();
            case BLOCK -> blockTokensDropFromBlocks.get();
            case ENTITY -> entityTokensDropFromEntities.get();
        };
    }
}
