package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SLLangProvider extends LanguageProvider {

    public SLLangProvider(PackOutput output) {
        super(output, StructureLoot.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.structureloot", "BBL Loot Generators");

        //Items
        add("item.structureloot.token_charm", "Token Charm");
        add("item.structureloot.structure_token", "Structure Token");
        add("item.structureloot.block_token", "Block Token");
        add("item.structureloot.entity_token", "Entity Token");
        add("item.structureloot.structure_loot_block", "Structure Loot Generator");

        //Blocks
        add("block.structureloot.structure_loot_block", "Loot Generator");
        add("block.structureloot.structure_loot_part", "Loot Generator");

        //Tooltips
        add("tooltip.structureloot.structure", "Structure: %s");
        add("tooltip.structureloot.block", "Block: %s");
        add("tooltip.structureloot.entity", "Entity: %s");
        add("tooltip.structureloot.structure_loot_block", "Places a 3x3x3 block that creates loot using Tokens and RF");
        add("tooltip.structureloot.structure_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.block_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.entity_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.token_charm", "When in inventory, valid blocks and entities have a chance to drop a Token");

    }

}


