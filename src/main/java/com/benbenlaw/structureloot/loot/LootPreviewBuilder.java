package com.benbenlaw.structureloot.loot;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.config.SLServerConfig;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.network.packet.LootPreviewPacket;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.RegistryOps;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;

import java.util.*;

public class LootPreviewBuilder {

    private static final int MAX_DEPTH = 4;
    private static final int MAX_TAG_ITEMS = 40;

    private final MinecraftServer server;
    private final RegistryOps<JsonElement> ops;

    private LootPreviewBuilder(MinecraftServer server) {
        this.server = server;
        this.ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }

    public static Map<Identifier, List<LootPreviewPacket.Drop>> build(MinecraftServer server) {
        LootPreviewBuilder builder = new LootPreviewBuilder(server);
        Map<Identifier, List<LootPreviewPacket.Drop>> result = new HashMap<>();

        for (RecipeHolder<?> holder : server.getRecipeManager().recipeMap().values()) {
            if (!(holder.value() instanceof StructureLootRecipe recipe)) continue;
            result.put(recipe.lootId(), builder.analyzeRecipe(recipe));
        }
        return result;
    }

    // ---------------------------------------------------------------- model

    private static final class Leaf {
        final Item item;
        ItemStack stack;
        List<ItemStack> variants = List.of();
        double chance;
        Map<Integer, Double> counts;
        List<Component> notes;

        Leaf(Item item, double chance, Map<Integer, Double> counts, List<Component> notes) {
            this.item = item;
            this.stack = new ItemStack(item);
            this.chance = chance;
            this.counts = counts;
            this.notes = notes;
        }

        Leaf copy() {
            Leaf leaf = new Leaf(item, chance, counts, new ArrayList<>(notes));
            leaf.stack = stack;
            leaf.variants = variants;
            return leaf;
        }
    }

    private record Cand(int weight, List<Leaf> leaves) {}

    private static final class Merged {
        double noneChance = 1.0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        ItemStack stack;
        List<ItemStack> variants;
        List<Component> notes;
    }

    // -------------------------------------------------------------- recipes

    private List<LootPreviewPacket.Drop> analyzeRecipe(StructureLootRecipe recipe) {
        Map<String, Merged> result = new LinkedHashMap<>();
        int tableCount = recipe.lootTables().size();
        Map<String, Double> chances = new HashMap<>();

        for (StructureLootRecipe.LootRoll roll : recipe.lootTables()) {
            JsonObject table = tableJson(roll.table());
            if (table == null) continue;

            Map<String, Merged> perTable = new LinkedHashMap<>();
            List<Leaf> leaves = new ArrayList<>(analyzeTable(table, 0));
            leaves.addAll(analyzeModifiers(roll.table()));
            for (Leaf leaf : leaves) {
                double nonZero = 1.0 - probabilityAtMost(leaf.counts, 0);
                double chance = leaf.chance * nonZero;
                if (chance <= 0) continue;

                String key = BuiltInRegistries.ITEM.getKey(leaf.stack.getItem()) + "|" + leaf.stack.getComponentsPatch().hashCode() + "|" + noteKey(leaf.notes);
                Merged m = perTable.computeIfAbsent(key, k -> {
                    Merged created = new Merged();
                    created.stack = leaf.stack;
                    created.variants = leaf.variants;
                    created.notes = leaf.notes;
                    return created;
                });
                m.noneChance *= (1.0 - chance);
                for (Map.Entry<Integer, Double> e : leaf.counts.entrySet()) {
                    if (e.getKey() > 0 && e.getValue() > 0) {
                        m.min = Math.min(m.min, e.getKey());
                        m.max = Math.max(m.max, e.getKey());
                    }
                }
            }

            for (Map.Entry<String, Merged> e : perTable.entrySet()) {
                Merged m = result.computeIfAbsent(e.getKey(), k -> {
                    Merged created = new Merged();
                    created.stack = e.getValue().stack;
                    created.variants = e.getValue().variants;
                    created.notes = e.getValue().notes;
                    return created;
                });
                m.min = Math.min(m.min, e.getValue().min);
                m.max = Math.max(m.max, e.getValue().max);
                chances.merge(e.getKey(), (1.0 - e.getValue().noneChance) / tableCount, Double::sum);
            }
        }

        return result.entrySet().stream()
                .filter(e -> e.getValue().max > 0)
                .sorted((a, b) -> Double.compare(chances.getOrDefault(b.getKey(), 0.0), chances.getOrDefault(a.getKey(), 0.0)))
                .map(e -> new LootPreviewPacket.Drop(e.getValue().stack, e.getValue().variants, e.getValue().min, e.getValue().max,
                        chances.getOrDefault(e.getKey(), 0.0).floatValue(), e.getValue().notes))
                .toList();
    }

    private static String noteKey(List<Component> notes) {
        StringBuilder sb = new StringBuilder();
        for (Component c : notes) sb.append(c.getString()).append('\n');
        return sb.toString();
    }

    private JsonObject tableJson(Identifier id) {
        LootTable table = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
        if (table == LootTable.EMPTY) return null;
        return LootTable.DIRECT_CODEC.encodeStart(ops, table).result()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .orElse(null);
    }

    // ------------------------------------------------------ global modifiers

    private enum Match { YES, NO, UNKNOWN }

    private List<Leaf> analyzeModifiers(Identifier tableId) {
        LootModifierManager manager = server.getServerResources().managers().getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
        if (manager == null) return List.of();

        List<Leaf> out = new ArrayList<>();
        for (IGlobalLootModifier modifier : manager.getSortedModifiers()) {
            JsonElement encoded = IGlobalLootModifier.DIRECT_CODEC.encodeStart(ops, modifier).result().orElse(null);
            if (encoded == null || !encoded.isJsonObject()) continue;
            JsonObject json = encoded.getAsJsonObject();

            String modifierType = path(json.get("type"));
            boolean tokenModifier = modifierType.equals("structure_loot_token");
            if (!tokenModifier && (!modifierType.equals("add_table") || !json.has("table"))) continue;

            List<Component> notes = new ArrayList<>();
            boolean applies = true;
            for (JsonObject condition : objects(json.get("conditions"))) {
                Match match = matchesTable(condition, tableId);
                if (match == Match.NO) {
                    applies = false;
                    break;
                }
                if (match == Match.UNKNOWN) notes.addAll(residualNotes(condition));
            }
            if (!applies) continue;

            if (tokenModifier) {
                Leaf token = tokenLeaf(tableId, json);
                if (token != null) {
                    token.notes.addAll(0, notes);
                    token.notes.add(0, Component.translatable("jei.structureloot.note.modifier", modName(StructureLoot.MOD_ID)));
                    token.notes = dedupe(token.notes);
                    out.add(token);
                }
                continue;
            }

            Identifier addedId = Identifier.tryParse(json.get("table").getAsString());
            JsonObject added = addedId == null ? null : tableJson(addedId);
            if (added == null) continue;

            Identifier modifierId = manager.getId(modifier);
            String namespace = modifierId != null ? modifierId.getNamespace() : addedId.getNamespace();
            Component source = Component.translatable("jei.structureloot.note.modifier", modName(namespace));

            for (Leaf leaf : analyzeTable(added, 1)) {
                leaf.notes.add(0, source);
                leaf.notes.addAll(1, notes);
                leaf.notes = dedupe(leaf.notes);
                out.add(leaf);
            }
        }
        return out;
    }

    private Leaf tokenLeaf(Identifier tableId, JsonObject modifier) {
        for (RecipeHolder<?> holder : server.getRecipeManager().recipeMap().values()) {
            if (!(holder.value() instanceof StructureLootRecipe recipe)) continue;

            for (StructureLootRecipe.LootRoll roll : recipe.lootTables()) {
                if (!roll.table().equals(tableId)) continue;
                double chance = recipe.obtainedChance();
                if (chance <= 0 || !SLServerConfig.isTokenEnabled(roll.type())) return null;

                Item tokenItem = switch (roll.type()) {
                    case GENERIC -> SLItems.STRUCTURE_TOKEN.get();
                    case BLOCK -> SLItems.BLOCK_TOKEN.get();
                    case ENTITY -> SLItems.ENTITY_TOKEN.get();
                };

                Leaf leaf = newLeaf(tokenItem, List.of());
                leaf.chance = chance;
                leaf.stack = new ItemStack(tokenItem);
                leaf.stack.set(SLDataComponents.LOOT_ID.get(), recipe.lootId());
                leaf.stack.set(DataComponents.MAX_DAMAGE, recipe.maxDurability());
                if (roll.type() != StructureLootRecipe.LootContextType.GENERIC) {
                    leaf.notes.add(Component.translatable("jei.structureloot.note.charm"));
                }
                return leaf;
            }
        }
        return null;
    }

    private static String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(c -> c.getModInfo().getDisplayName())
                .orElse(namespace);
    }

    private static boolean mentionsTableId(JsonElement e) {
        return e.toString().contains("loot_table_id");
    }

    private Match matchesTable(JsonObject c, Identifier tableId) {
        String type = path(c.get("condition"));
        switch (type) {
            case "loot_table_id" -> {
                return c.has("loot_table_id") && c.get("loot_table_id").getAsString().equals(tableId.toString()) ? Match.YES : Match.NO;
            }
            case "inverted" -> {
                if (c.has("term") && c.get("term").isJsonObject()) {
                    Match inner = matchesTable(c.getAsJsonObject("term"), tableId);
                    return inner == Match.YES ? Match.NO : inner == Match.NO ? Match.YES : Match.UNKNOWN;
                }
            }
            case "any_of" -> {
                boolean allNo = true;
                for (JsonObject term : objects(c.get("terms"))) {
                    Match m = matchesTable(term, tableId);
                    if (m == Match.YES) return Match.YES;
                    if (m != Match.NO) allNo = false;
                }
                return allNo ? Match.NO : Match.UNKNOWN;
            }
            case "all_of" -> {
                boolean allYes = true;
                for (JsonObject term : objects(c.get("terms"))) {
                    Match m = matchesTable(term, tableId);
                    if (m == Match.NO) return Match.NO;
                    if (m != Match.YES) allYes = false;
                }
                return allYes ? Match.YES : Match.UNKNOWN;
            }
            default -> {}
        }
        return Match.UNKNOWN;
    }

    private List<Component> residualNotes(JsonObject c) {
        if (!mentionsTableId(c)) {
            Component described = describeCondition(c);
            return described == null ? List.of() : List.of(described);
        }

        String type = path(c.get("condition"));
        List<Component> out = new ArrayList<>();
        if (type.equals("all_of") || type.equals("any_of")) {
            for (JsonObject term : objects(c.get("terms"))) {
                if (path(term.get("condition")).equals("loot_table_id")) continue;
                out.addAll(residualNotes(term));
            }
        }
        return out;
    }

    // --------------------------------------------------------------- tables

    private List<Leaf> analyzeTable(JsonObject table, int depth) {
        List<Leaf> out = new ArrayList<>();
        List<JsonObject> tableFunctions = objects(table.get("functions"));

        for (JsonObject pool : objects(table.get("pools"))) {
            out.addAll(analyzePool(pool, tableFunctions, depth));
        }
        return out;
    }

    private List<Leaf> analyzePool(JsonObject pool, List<JsonObject> tableFunctions, int depth) {
        List<Component> poolNotes = conditionNotes(pool.get("conditions"));
        List<JsonObject> poolFunctions = objects(pool.get("functions"));

        List<Cand> cands = new ArrayList<>();
        for (JsonObject entry : objects(pool.get("entries"))) {
            cands.addAll(collect(entry, List.of(), depth));
        }

        int total = cands.stream().mapToInt(Cand::weight).sum();
        if (total <= 0) return List.of();

        Map<Integer, Double> rolls = numberDist(pool.get("rolls"));
        if (rolls == null) rolls = Map.of(1, 1.0);

        boolean bonusRolls = pool.has("bonus_rolls") && !isZero(pool.get("bonus_rolls"));

        List<Leaf> out = new ArrayList<>();
        for (Cand cand : cands) {
            double pick = cand.weight() / (double) total;
            for (Leaf source : cand.leaves()) {
                Leaf leaf = source.copy();
                double perRoll = pick * leaf.chance;
                double overall = 0;
                for (Map.Entry<Integer, Double> r : rolls.entrySet()) {
                    overall += r.getValue() * (1.0 - Math.pow(1.0 - perRoll, r.getKey()));
                }
                leaf.chance = overall;

                List<Leaf> finished = new ArrayList<>();
                for (Leaf l : applyFunctions(leaf, poolFunctions)) finished.addAll(applyFunctions(l, tableFunctions));

                for (Leaf l : finished) {
                    List<Component> notes = new ArrayList<>(poolNotes);
                    notes.addAll(l.notes);
                    if (bonusRolls) notes.add(Component.translatable("jei.structureloot.note.bonus_rolls"));
                    l.notes = dedupe(notes);
                    out.add(l);
                }
            }
        }
        return out;
    }

    // -------------------------------------------------------------- entries

    private List<Cand> collect(JsonObject entry, List<Component> inherited, int depth) {
        String type = path(entry.get("type"));
        List<Component> own = new ArrayList<>(inherited);
        own.addAll(conditionNotes(entry.get("conditions")));

        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
        List<JsonObject> functions = objects(entry.get("functions"));
        if (entry.has("quality") && entry.get("quality").getAsInt() != 0) {
            own.add(Component.translatable("jei.structureloot.note.luck"));
        }

        switch (type) {
            case "item" -> {
                Item item = itemOf(entry.get("name"));
                if (item == null) return List.of();
                Leaf leaf = newLeaf(item, own);
                return List.of(new Cand(weight, applyFunctions(leaf, functions)));
            }
            case "tag" -> {
                Identifier tagId = Identifier.tryParse(entry.get("name").getAsString());
                if (tagId == null) return List.of();
                List<Item> items = new ArrayList<>();
                for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, tagId))) {
                    if (items.size() >= MAX_TAG_ITEMS) break;
                    items.add(holder.value());
                }
                boolean expand = entry.has("expand") && entry.get("expand").getAsBoolean();

                List<Cand> out = new ArrayList<>();
                List<Leaf> shared = new ArrayList<>();
                for (Item item : items) {
                    Leaf leaf = newLeaf(item, own);
                    if (!expand) {
                        leaf.chance = 1.0 / items.size();
                        leaf.notes.add(Component.translatable("jei.structureloot.note.tag", "#" + tagId));
                    }
                    List<Leaf> result = applyFunctions(leaf, functions);
                    if (expand) out.add(new Cand(weight, result));
                    else shared.addAll(result);
                }
                if (!expand && !shared.isEmpty()) out.add(new Cand(weight, shared));
                return out;
            }
            case "loot_table" -> {
                if (depth >= MAX_DEPTH) return List.of();
                JsonObject sub = nestedTable(entry.get("value"));
                if (sub == null) return List.of();
                List<Leaf> leaves = new ArrayList<>();
                for (Leaf leaf : analyzeTable(sub, depth + 1)) {
                    leaf.notes.addAll(0, own);
                    leaves.addAll(applyFunctions(leaf, functions));
                }
                return List.of(new Cand(weight, leaves));
            }
            case "alternatives" -> {
                List<Leaf> leaves = new ArrayList<>();
                int altWeight = -1;
                List<JsonObject> children = objects(entry.get("children"));
                for (int i = 0; i < children.size(); i++) {
                    List<Component> childNotes = new ArrayList<>(own);
                    if (i > 0) childNotes.add(Component.translatable("jei.structureloot.note.fallback"));
                    for (Cand c : collect(children.get(i), childNotes, depth)) {
                        if (altWeight < 0) altWeight = c.weight();
                        leaves.addAll(c.leaves());
                    }
                }
                return leaves.isEmpty() ? List.of() : List.of(new Cand(Math.max(altWeight, 1), leaves));
            }
            case "group", "sequence" -> {
                List<Cand> out = new ArrayList<>();
                for (JsonObject child : objects(entry.get("children"))) {
                    out.addAll(collect(child, own, depth));
                }
                return out;
            }
            case "empty" -> {
                return List.of(new Cand(weight, List.of()));
            }
            default -> {
                return List.of();
            }
        }
    }

    private JsonObject nestedTable(JsonElement value) {
        if (value == null) return null;
        if (value.isJsonObject()) return value.getAsJsonObject();
        if (value.isJsonPrimitive()) {
            Identifier id = Identifier.tryParse(value.getAsString());
            return id == null ? null : tableJson(id);
        }
        return null;
    }

    private static Item itemOf(JsonElement name) {
        if (name == null) return null;
        Identifier id = Identifier.tryParse(name.getAsString());
        return id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }

    private static Leaf newLeaf(Item item, List<Component> notes) {
        Map<Integer, Double> counts = new TreeMap<>();
        counts.put(1, 1.0);
        return new Leaf(item, 1.0, counts, new ArrayList<>(notes));
    }

    // ------------------------------------------------------------ functions

    private List<Leaf> applyFunctions(Leaf leaf, List<JsonObject> functions) {
        List<Leaf> current = new ArrayList<>(List.of(leaf));
        for (JsonObject fn : functions) {
            List<Leaf> next = new ArrayList<>();
            for (Leaf l : current) next.addAll(applyFunction(l, fn));
            current = next;
        }
        for (Leaf l : current) l.notes = dedupe(l.notes);
        return current;
    }

    private List<Leaf> applyFunction(Leaf leaf, JsonObject fn) {
        {
            String type = path(fn.get("function"));
            List<Component> conditions = conditionNotes(fn.get("conditions"));

            switch (type) {
                case "enchant_randomly" -> {
                    boolean wasBook = leaf.stack.is(Items.BOOK);
                    leaf.stack = asEnchantable(leaf.stack);
                    if (wasBook && conditions.isEmpty()) {
                        List<Leaf> expanded = expandRandomEnchant(leaf, fn);
                        if (!expanded.isEmpty()) return expanded;
                    }
                    Component note = fn.has("options")
                            ? Component.translatable("jei.structureloot.fn.enchant_random_from", compact(fn.get("options")))
                            : Component.translatable("jei.structureloot.fn.enchant_random");
                    leaf.notes.add(conditionalNote(note, conditions));
                }
                case "enchant_with_levels" -> {
                    boolean wasBook = leaf.stack.is(Items.BOOK);
                    leaf.stack = asEnchantable(leaf.stack);
                    if (wasBook && conditions.isEmpty()) {
                        List<Leaf> expanded = expandLevelEnchant(leaf, fn);
                        if (!expanded.isEmpty()) return expanded;
                    }
                    leaf.notes.add(conditionalNote(Component.translatable("jei.structureloot.fn.enchant_levels", describeNumber(fn.get("levels"))), conditions));
                }
                case "set_enchantments" -> {
                    leaf.stack = asEnchantable(leaf.stack);
                    boolean applied = conditions.isEmpty() && setEnchantments(leaf, fn.get("enchantments"));
                    if (!applied) {
                        leaf.notes.add(conditionalNote(Component.translatable("jei.structureloot.fn.enchant_set",
                                fn.has("enchantments") ? compact(fn.get("enchantments")) : "?"), conditions));
                    }
                }
                case "explosion_decay", "set_components", "set_lore", "set_name", "copy_name", "copy_components" -> {}
                case "set_count" -> {
                    Map<Integer, Double> dist = numberDist(fn.get("count"));
                    boolean add = fn.has("add") && fn.get("add").getAsBoolean();
                    if (dist == null || !conditions.isEmpty()) {
                        leaf.notes.add(conditionalNote(Component.translatable("jei.structureloot.fn.count", describeNumber(fn.get("count"))), conditions));
                    } else {
                        leaf.counts = add ? convolve(leaf.counts, dist) : dist;
                    }
                }
                case "limit_count" -> {
                    JsonObject limit = fn.has("limit") && fn.get("limit").isJsonObject() ? fn.getAsJsonObject("limit") : null;
                    Integer min = limit != null && limit.has("min") ? constantInt(limit.get("min")) : null;
                    Integer max = limit != null && limit.has("max") ? constantInt(limit.get("max")) : null;
                    if (conditions.isEmpty() && (min != null || max != null)) {
                        Map<Integer, Double> clamped = new TreeMap<>();
                        for (Map.Entry<Integer, Double> e : leaf.counts.entrySet()) {
                            int v = e.getKey();
                            if (min != null) v = Math.max(v, min);
                            if (max != null) v = Math.min(v, max);
                            clamped.merge(v, e.getValue(), Double::sum);
                        }
                        leaf.counts = clamped;
                    }
                }
                case "enchanted_count_increase" -> leaf.notes.add(conditionalNote(Component.translatable("jei.structureloot.fn.per_level",
                        describeNumber(fn.get("count")), enchantmentName(fn.get("enchantment"))), conditions));
                case "apply_bonus" -> leaf.notes.add(conditionalNote(Component.translatable("jei.structureloot.fn.bonus",
                        enchantmentName(fn.get("enchantment"))), conditions));
                default -> leaf.notes.add(conditionalNote(Component.literal(pretty(type)), conditions));
            }
        }
        return List.of(leaf);
    }

    private List<Holder<Enchantment>> enchantmentOptions(JsonElement options) {
        var lookup = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> out = new ArrayList<>();

        if (options == null) {
            lookup.listElements().forEach(out::add);
        } else if (options.isJsonPrimitive()) {
            addOption(lookup, options.getAsString(), out);
        } else if (options.isJsonArray()) {
            options.getAsJsonArray().forEach(e -> addOption(lookup, e.getAsString(), out));
        }
        return out;
    }

    private static void addOption(HolderLookup.RegistryLookup<Enchantment> lookup, String value, List<Holder<Enchantment>> out) {
        boolean tag = value.startsWith("#");
        Identifier id = Identifier.tryParse(tag ? value.substring(1) : value);
        if (id == null) return;

        if (tag) {
            lookup.get(TagKey.create(Registries.ENCHANTMENT, id)).ifPresent(set -> set.forEach(out::add));
        } else {
            lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id)).ifPresent(out::add);
        }
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return book;
    }

    private static Leaf enchantedLeaf(Leaf base, Holder<Enchantment> enchantment, int minLevel, int maxLevel, double share) {
        Leaf leaf = base.copy();
        leaf.chance = base.chance * share;
        List<ItemStack> variants = new ArrayList<>();
        for (int level = minLevel; level <= maxLevel; level++) variants.add(enchantedBook(enchantment, level));
        leaf.stack = variants.get(0);
        leaf.variants = variants;
        leaf.notes = new ArrayList<>(base.notes);
        leaf.notes.add(Component.translatable("jei.structureloot.note.enchant_level_range", minLevel, maxLevel));
        return leaf;
    }

    private List<Leaf> expandRandomEnchant(Leaf base, JsonObject fn) {
        List<Holder<Enchantment>> options = enchantmentOptions(fn.get("options"));
        List<Leaf> out = new ArrayList<>();
        for (Holder<Enchantment> holder : options) {
            Enchantment e = holder.value();
            out.add(enchantedLeaf(base, holder, e.getMinLevel(), e.getMaxLevel(), 1.0 / options.size()));
        }
        return out;
    }

    private List<Leaf> expandLevelEnchant(Leaf base, JsonObject fn) {
        Map<Integer, Double> levels = numberDist(fn.get("levels"));
        if (levels == null || levels.isEmpty()) return List.of();

        var enchantable = new ItemStack(Items.BOOK).get(DataComponents.ENCHANTABLE);
        int enchantValue = enchantable == null ? 1 : enchantable.value();
        int low = (int) Math.floor((Collections.min(levels.keySet()) + 1) * 0.85);
        int high = (int) Math.ceil((Collections.max(levels.keySet()) + 1 + enchantValue / 2) * 1.15);

        ItemStack book = new ItemStack(Items.BOOK);
        List<Holder<Enchantment>> holders = new ArrayList<>();
        List<int[]> ranges = new ArrayList<>();
        double totalWeight = 0;

        for (Holder<Enchantment> holder : enchantmentOptions(fn.get("options"))) {
            if (!book.isPrimaryItemFor(holder)) continue;
            Enchantment e = holder.value();

            int minLevel = -1;
            int maxLevel = -1;
            for (int level = e.getMinLevel(); level <= e.getMaxLevel(); level++) {
                if (high >= e.getMinCost(level) && low <= e.getMaxCost(level)) {
                    if (minLevel < 0) minLevel = level;
                    maxLevel = level;
                }
            }
            if (minLevel < 0) continue;

            holders.add(holder);
            ranges.add(new int[]{minLevel, maxLevel});
            totalWeight += e.getWeight();
        }

        List<Leaf> out = new ArrayList<>();
        for (int i = 0; i < holders.size(); i++) {
            Leaf leaf = enchantedLeaf(base, holders.get(i), ranges.get(i)[0], ranges.get(i)[1], holders.get(i).value().getWeight() / totalWeight);
            leaf.notes.add(Component.translatable("jei.structureloot.note.enchant_extra"));
            out.add(leaf);
        }
        return out;
    }

    private static ItemStack asEnchantable(ItemStack stack) {
        return stack.is(Items.BOOK) ? new ItemStack(Items.ENCHANTED_BOOK) : stack;
    }

    private boolean setEnchantments(Leaf leaf, JsonElement enchantments) {
        if (enchantments == null || !enchantments.isJsonObject()) return false;

        var lookup = server.registryAccess().lookup(Registries.ENCHANTMENT).orElse(null);
        if (lookup == null) return false;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<String, JsonElement> e : enchantments.getAsJsonObject().entrySet()) {
            Identifier id = Identifier.tryParse(e.getKey());
            Integer level = constantInt(e.getValue());
            if (id == null || level == null) return false;

            var holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id)).orElse(null);
            if (holder == null) return false;
            mutable.set(holder, level);
        }

        leaf.stack = leaf.stack.copy();
        leaf.stack.set(leaf.stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return true;
    }

    private static Component conditionalNote(Component base, List<Component> conditions) {
        if (conditions.isEmpty()) return base;
        var joined = Component.empty();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) joined.append(", ");
            joined.append(conditions.get(i));
        }
        return Component.translatable("jei.structureloot.note.if", base, joined);
    }

    // ----------------------------------------------------------- conditions

    private List<Component> conditionNotes(JsonElement conditions) {
        List<Component> out = new ArrayList<>();
        for (JsonObject c : objects(conditions)) {
            Component described = describeCondition(c);
            if (described != null) out.add(described);
        }
        return out;
    }

    private Component describeCondition(JsonObject c) {
        String type = path(c.get("condition"));
        switch (type) {
            case "survives_explosion" -> {
                return null;
            }
            case "killed_by_player" -> {
                return Component.translatable("jei.structureloot.cond.killed_by_player");
            }
            case "random_chance" -> {
                return Component.translatable("jei.structureloot.cond.random_chance", percent(numberValue(c.get("chance"))));
            }
            case "random_chance_with_enchanted_bonus" -> {
                double base = numberValue(c.get("unenchanted_chance"));
                double first = base;
                double perLevel = 0;
                JsonElement enchanted = c.get("enchanted_chance");
                if (enchanted != null && enchanted.isJsonObject() && enchanted.getAsJsonObject().has("base")) {
                    JsonObject linear = enchanted.getAsJsonObject();
                    first = numberValue(linear.get("base"));
                    perLevel = linear.has("per_level_above_first") ? numberValue(linear.get("per_level_above_first")) : 0;
                } else if (enchanted != null) {
                    first = numberValue(enchanted);
                }
                return Component.translatable("jei.structureloot.cond.random_chance_bonus",
                        percent(base), enchantmentName(c.get("enchantment")), percent(first), percent(perLevel));
            }
            case "match_tool" -> {
                String json = c.toString();
                if (json.contains("silk_touch")) return Component.translatable("jei.structureloot.cond.silk_touch");
                if (json.contains("shears")) return Component.translatable("jei.structureloot.cond.shears");
            }
            case "inverted" -> {
                if (c.has("term") && c.get("term").isJsonObject()) {
                    Component inner = describeCondition(c.getAsJsonObject("term"));
                    if (inner != null) return Component.translatable("jei.structureloot.cond.not", inner);
                }
            }
            case "any_of", "all_of" -> {
                var joined = Component.empty();
                boolean first = true;
                for (JsonObject term : objects(c.get("terms"))) {
                    Component inner = describeCondition(term);
                    if (inner == null) continue;
                    if (!first) joined.append(type.equals("any_of") ? " / " : " + ");
                    joined.append(inner);
                    first = false;
                }
                return first ? null : Component.translatable("jei.structureloot.cond." + type, joined);
            }
            case "table_bonus" -> {
                List<String> chances = new ArrayList<>();
                if (c.has("chances") && c.get("chances").isJsonArray()) {
                    c.getAsJsonArray("chances").forEach(e -> chances.add(percent(e.getAsDouble())));
                }
                return Component.translatable("jei.structureloot.cond.table_bonus", enchantmentName(c.get("enchantment")), String.join(", ", chances));
            }
            case "block_state_property" -> {
                String block = c.has("block") ? pretty(path(c.get("block"))) : "?";
                String props = c.has("properties") ? compact(c.get("properties")) : "";
                return Component.translatable("jei.structureloot.cond.block_state", block, props);
            }
            case "weather_check" -> {
                if (c.has("raining")) return Component.translatable(c.get("raining").getAsBoolean()
                        ? "jei.structureloot.cond.raining" : "jei.structureloot.cond.not_raining");
                if (c.has("thundering")) return Component.translatable(c.get("thundering").getAsBoolean()
                        ? "jei.structureloot.cond.thundering" : "jei.structureloot.cond.not_thundering");
            }
            case "reference" -> {
                return Component.translatable("jei.structureloot.cond.reference", c.has("name") ? c.get("name").getAsString() : "?");
            }
            default -> {}
        }

        JsonObject rest = c.deepCopy();
        rest.remove("condition");
        String detail = compact(rest);
        return Component.literal(pretty(type) + (detail.isEmpty() ? "" : ": " + detail));
    }

    // -------------------------------------------------------------- numbers

    private static Map<Integer, Double> numberDist(JsonElement e) {
        if (e == null) return null;
        Double constant = constantValue(e);
        if (constant != null) return singleton((int) Math.round(constant));
        if (!e.isJsonObject()) return null;

        JsonObject o = e.getAsJsonObject();
        switch (path(o.get("type"))) {
            case "uniform" -> {
                Double min = constantValue(o.get("min"));
                Double max = constantValue(o.get("max"));
                if (min == null || max == null) return null;
                int a = (int) Math.round(min);
                int b = (int) Math.round(max);
                if (a >= b) return singleton(a);
                Map<Integer, Double> dist = new TreeMap<>();
                for (int i = a; i <= b; i++) dist.put(i, 1.0 / (b - a + 1));
                return dist;
            }
            case "binomial" -> {
                Double n = constantValue(o.get("n"));
                Double p = constantValue(o.get("p"));
                if (n == null || p == null || n > 64) return null;
                int trials = (int) Math.round(n);
                Map<Integer, Double> dist = new TreeMap<>();
                for (int k = 0; k <= trials; k++) {
                    dist.put(k, choose(trials, k) * Math.pow(p, k) * Math.pow(1 - p, trials - k));
                }
                return dist;
            }
            default -> {
                return null;
            }
        }
    }

    private static Double constantValue(JsonElement e) {
        if (e == null) return null;
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) return e.getAsDouble();
        if (e.isJsonObject() && path(e.getAsJsonObject().get("type")).equals("constant")) {
            return constantValue(e.getAsJsonObject().get("value"));
        }
        return null;
    }

    private static Integer constantInt(JsonElement e) {
        Double d = constantValue(e);
        return d == null ? null : (int) Math.round(d);
    }

    private static double numberValue(JsonElement e) {
        Double d = constantValue(e);
        return d == null ? 0 : d;
    }

    private static boolean isZero(JsonElement e) {
        Double d = constantValue(e);
        return d != null && d == 0;
    }

    private static String describeNumber(JsonElement e) {
        Map<Integer, Double> dist = numberDist(e);
        if (dist == null || dist.isEmpty()) return "?";
        int min = Collections.min(dist.keySet());
        int max = Collections.max(dist.keySet());
        return min == max ? String.valueOf(min) : min + "-" + max;
    }

    private static Map<Integer, Double> singleton(int value) {
        Map<Integer, Double> m = new TreeMap<>();
        m.put(value, 1.0);
        return m;
    }

    private static Map<Integer, Double> convolve(Map<Integer, Double> a, Map<Integer, Double> b) {
        Map<Integer, Double> out = new TreeMap<>();
        for (Map.Entry<Integer, Double> x : a.entrySet()) {
            for (Map.Entry<Integer, Double> y : b.entrySet()) {
                out.merge(x.getKey() + y.getKey(), x.getValue() * y.getValue(), Double::sum);
            }
        }
        return out;
    }

    private static double probabilityAtMost(Map<Integer, Double> dist, int value) {
        double sum = 0;
        for (Map.Entry<Integer, Double> e : dist.entrySet()) {
            if (e.getKey() <= value) sum += e.getValue();
        }
        return sum;
    }

    private static double choose(int n, int k) {
        double result = 1;
        for (int i = 1; i <= k; i++) result = result * (n - k + i) / i;
        return result;
    }

    // -------------------------------------------------------------- helpers

    private static List<JsonObject> objects(JsonElement e) {
        List<JsonObject> out = new ArrayList<>();
        if (e != null && e.isJsonArray()) {
            for (JsonElement el : (JsonArray) e) {
                if (el.isJsonObject()) out.add(el.getAsJsonObject());
            }
        }
        return out;
    }

    private static String path(JsonElement e) {
        if (e == null || !e.isJsonPrimitive()) return "";
        String s = e.getAsString();
        int i = s.indexOf(':');
        return i >= 0 ? s.substring(i + 1) : s;
    }

    private static Component enchantmentName(JsonElement e) {
        if (e == null || !e.isJsonPrimitive()) return Component.literal("?");
        Identifier id = Identifier.tryParse(e.getAsString());
        if (id == null) return Component.literal(e.getAsString());
        return Component.translatable("enchantment." + id.getNamespace() + "." + id.getPath());
    }

    private static String percent(double fraction) {
        double p = fraction * 100.0;
        if (Math.abs(p - Math.rint(p)) < 0.005) return String.valueOf((long) Math.rint(p));
        return String.format("%.1f", p);
    }

    private static String pretty(String path) {
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("[_/]")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private static String compact(JsonElement e) {
        String s = compactRaw(e).replace("minecraft:", "");
        return s.length() > 90 ? s.substring(0, 87) + "..." : s;
    }

    private static String compactRaw(JsonElement e) {
        if (e == null || e.isJsonNull()) return "";
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            return p.getAsString();
        }
        if (e.isJsonArray()) {
            StringJoiner j = new StringJoiner(", ", "[", "]");
            e.getAsJsonArray().forEach(x -> j.add(compactRaw(x)));
            return j.toString();
        }
        StringJoiner j = new StringJoiner(", ");
        e.getAsJsonObject().entrySet().forEach(x -> j.add(x.getKey() + "=" + compactRaw(x.getValue())));
        return j.toString();
    }

    private static List<Component> dedupe(List<Component> notes) {
        Map<String, Component> seen = new LinkedHashMap<>();
        for (Component c : notes) seen.putIfAbsent(c.getString(), c);
        return new ArrayList<>(seen.values());
    }
}
