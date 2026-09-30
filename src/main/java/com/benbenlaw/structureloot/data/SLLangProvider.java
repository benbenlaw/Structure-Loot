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
        add("item.structureloot.structure_token", "Structure Token: %s");
        add("item.structureloot.block_token", "Block Token: %s");
        add("item.structureloot.entity_token", "Entity Token: %s");
        add("item.structureloot.structure_loot_block", "Structure Loot Generator");

        //Blocks
        add("block.structureloot.structure_loot_block", "Loot Generator");
        add("block.structureloot.structure_loot_part", "Loot Generator");

        //JEI
        add("jei.structureloot.loot_generator", "Loot Generator");
        add("jei.structureloot.summary", "%s rolls, %ss, %s RF/t");
        add("jei.structureloot.count", "Count: %s");
        add("jei.structureloot.chance", "Chance per roll: %s");
        add("jei.structureloot.note.enchant_level_range", "Level %s-%s");
        add("jei.structureloot.note.enchant_extra", "May have extra enchantments");
        add("jei.structureloot.fn.enchant_random", "Random enchantment");
        add("jei.structureloot.fn.enchant_random_from", "Random enchantment from %s");
        add("jei.structureloot.fn.enchant_levels", "Enchanted at level %s");
        add("jei.structureloot.fn.enchant_set", "Enchantments: %s");
        add("jei.structureloot.note.modifier", "Added by %s");
        add("jei.structureloot.note.charm", "Requires a Token Charm on the player");
        add("jei.structureloot.note.bonus_rolls", "Extra rolls with Luck");
        add("jei.structureloot.note.luck", "Weight affected by Luck");
        add("jei.structureloot.note.tag", "One random item from %s");
        add("jei.structureloot.note.fallback", "Only if the earlier options don't apply");
        add("jei.structureloot.note.if", "%s (if %s)");
        add("jei.structureloot.fn.count", "Count: %s");
        add("jei.structureloot.fn.per_level", "+%s per %s level");
        add("jei.structureloot.fn.bonus", "Increased by %s");
        add("jei.structureloot.cond.killed_by_player", "Needs an item in the upgrade slot (killed by a player)");
        add("jei.structureloot.cond.random_chance", "%s%% chance");
        add("jei.structureloot.cond.random_chance_bonus", "%s%% chance (%s%% with %s, +%s%% per extra level)");
        add("jei.structureloot.cond.silk_touch", "Mined with Silk Touch");
        add("jei.structureloot.cond.shears", "Mined with Shears");
        add("jei.structureloot.cond.not", "Not: %s");
        add("jei.structureloot.cond.any_of", "Any of: %s");
        add("jei.structureloot.cond.all_of", "All of: %s");
        add("jei.structureloot.cond.table_bonus", "%s chances by level: %s%%");
        add("jei.structureloot.cond.block_state", "Block %s (%s)");
        add("jei.structureloot.cond.raining", "While raining");
        add("jei.structureloot.cond.not_raining", "While not raining");
        add("jei.structureloot.cond.thundering", "While thundering");
        add("jei.structureloot.cond.not_thundering", "While not thundering");
        add("jei.structureloot.cond.reference", "Predicate %s");

        //Tooltips
        add("tooltip.structureloot.structure", "Structure: %s");
        add("tooltip.structureloot.block", "Block: %s");
        add("tooltip.structureloot.entity", "Entity: %s");
        add("tooltip.structureloot.structure_loot_block", "Places a 3x3x3 block that creates loot using Tokens and RF. Top slot is for Tokens and the bottom is for the item you want the Generator to hold");
        add("tooltip.structureloot.structure_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.block_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.entity_token", "Used inside the Loot Generator to create loot!");
        add("tooltip.structureloot.token_charm", "When in inventory, valid blocks and entities have a chance to drop a Token");

    }

}


