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
        add("itemGroup.structureloot", "Structure Loot");

        //Items
        add("item.structureloot.structure_token", "Structure Token");
        add("item.structureloot.structure_loot_block", "Structure Loot Generator");

        //Blocks
        add("block.structureloot.structure_loot_block", "Structure Loot Generator");
        add("block.structureloot.structure_loot_part", "Structure Loot Generator");

        //Tooltips
        add("tooltip.structureloot.structure", "Structure: %s");
        add("tooltip.structureloot.structure_loot_block", "Places a 3x3x3 block that creates loot from structures using Structure Tokens and RF");
        add("tooltip.structureloot.structure_token", "Used inside the Structure Loot Generator to create loot!");

    }

}


