package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.benbenlaw.structureloot.data.custom.StructureLootRecipeBuilder.structureLootRecipe;

public class SLRecipeProvider extends RecipeProvider {

    public SLRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
            super(packOutput, provider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput recipeOutput) {
            return new SLRecipeProvider(provider, recipeOutput);
        }

        @Override
        public @NotNull String getName() {
            return StructureLoot.MOD_ID + " Recipes";
        }
    }

    @Override
    protected void buildRecipes() {

        shaped(RecipeCategory.MISC, SLBlocks.STRUCTURE_LOOT_BLOCK.asItem(), 1)
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', Blocks.STONE_BRICKS)
                .define('B', ItemTags.LOGS)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("has_structure_loot_token", has(Tags.Items.CHESTS_WOODEN))
                .save(output);

        simpleLootRecipe("simple_dungeon", "chests/simple_dungeon");

        simpleLootRecipe("mineshaft", "chests/abandoned_mineshaft");

        simpleLootRecipe("stronghold", 1, 1000, 2000,
                "chests/stronghold_corridor",
                "chests/stronghold_crossing",
                "chests/stronghold_library"
        );

        simpleLootRecipe("desert_pyramid", "chests/desert_pyramid");

        simpleLootRecipe("jungle_temple", "chests/jungle_temple");

        simpleLootRecipe("igloo", "chests/igloo_chest");

        simpleLootRecipe("woodland_mansion", "chests/woodland_mansion");

        simpleLootRecipe("shipwreck", 1, 1000, 1000,
                "chests/shipwreck_supply",
                "chests/shipwreck_treasure",
                "chests/shipwreck_map"
        );

        simpleLootRecipe("buried_treasure", "chests/buried_treasure");

        simpleLootRecipe("ocean_ruin", 1, 800, 800,
                "chests/underwater_ruin_big",
                "chests/underwater_ruin_small"
        );

        simpleLootRecipe("pillager_outpost", "chests/pillager_outpost");

        simpleLootRecipe("village_common", 3, 600, 1000,
                "chests/village/village_armorer",
                "chests/village/village_butcher",
                "chests/village/village_cartographer",
                "chests/village/village_fisher",
                "chests/village/village_fletcher",
                "chests/village/village_mason",
                "chests/village/village_shepherd",
                "chests/village/village_tannery",
                "chests/village/village_temple",
                "chests/village/village_toolsmith",
                "chests/village/village_weaponsmith"
        );

        simpleLootRecipe("village_desert",
                "chests/village/village_desert_house"
        );

        simpleLootRecipe("village_plains",
                "chests/village/village_plains_house"
        );

        simpleLootRecipe("village_savanna",
                "chests/village/village_savanna_house"
        );

        simpleLootRecipe("village_snowy",
                "chests/village/village_snowy_house"
        );

        simpleLootRecipe("village_taiga",
                "chests/village/village_taiga_house"
        );

        simpleLootRecipe("ancient_city", 1, 1200, 1600,
                "chests/ancient_city",
                "chests/ancient_city_ice_box"
        );

        simpleLootRecipe("bastion", 3, 1200, 1600,
                "chests/bastion_bridge",
                "chests/bastion_hoglin_stable",
                "chests/bastion_housing",
                "chests/bastion_other",
                "chests/bastion_treasure"
        );

        simpleLootRecipe("nether_fortress", "chests/nether_bridge");

        simpleLootRecipe("ruined_portal", "chests/ruined_portal");

        simpleLootRecipe("end_city", "chests/end_city_treasure");

        simpleLootRecipe("trial_chambers", 5, 1000, 1000,
                "chests/trial_chambers/entrance",
                "chests/trial_chambers/corridor",
                "chests/trial_chambers/intersection",
                "chests/trial_chambers/supply",
                "chests/trial_chambers/reward",
                "chests/trial_chambers/reward_unique",
                "chests/trial_chambers/reward_ominous_unique"
        );
    }

    public void simpleLootRecipe(String structure, String... lootTable) {

        List<Identifier> lootTables = Stream.of(lootTable).map(Identifier::withDefaultNamespace).toList();

        structureLootRecipe(Identifier.withDefaultNamespace(structure), lootTables, 2, 400, 600).save(output);
    }

    public void simpleLootRecipe(String structure, int rolls, int duration, int rfPerTick, String... lootTable) {

        List<Identifier> lootTables = Stream.of(lootTable).map(Identifier::withDefaultNamespace).toList();

        structureLootRecipe(Identifier.withDefaultNamespace(structure), lootTables, rolls, duration, rfPerTick).save(output);
    }

}