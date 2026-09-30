package com.benbenlaw.structureloot.integration;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.network.packet.LootPreviewPacket;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LootGeneratorCategory implements IRecipeCategory<LootGeneratorCategory.Entry> {

    public static final IRecipeType<Entry> TYPE = IRecipeType.create(StructureLoot.MOD_ID, "loot_generator", Entry.class);

    private static final int WIDTH = 142;
    private static final int HEIGHT = 104;
    private static final int FOOTER_HEIGHT = 12;
    private static final int COLUMNS = 7;
    private static final int VISIBLE_ROWS = 4;

    public record Entry(ItemStack token, StructureLootRecipe recipe, List<LootPreviewPacket.Drop> drops) {}

    private final IDrawable icon;

    public LootGeneratorCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(SLItems.STRUCTURE_LOOT_BLOCK.get());
    }

    @Override
    public IRecipeType<Entry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.structureloot.loot_generator");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        builder.addInputSlot()
                .add(entry.token())
                .setStandardSlotBackground();

        for (LootPreviewPacket.Drop drop : entry.drops()) {
            IRecipeSlotBuilder slot = builder.addOutputSlot();
            if (drop.variants().isEmpty()) slot.add(drop.stack());
            else drop.variants().forEach(slot::add);

            slot.addRichTooltipCallback((view, tooltip) -> {
                String count = drop.min() == drop.max() ? String.valueOf(drop.min()) : drop.min() + "-" + drop.max();
                tooltip.add(Component.translatable("jei.structureloot.count", count).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("jei.structureloot.chance", formatChance(drop.chance())).withStyle(ChatFormatting.GRAY));
                for (Component note : drop.notes()) {
                    tooltip.add(Component.literal("- ").append(note).withStyle(ChatFormatting.DARK_AQUA));
                }
            });
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Entry entry, IFocusGroup focuses) {
        List<FormattedText> text = List.of(
                entry.token().getHoverName(),
                Component.literal(entry.recipe().lootId().toString()).withStyle(ChatFormatting.GRAY)
        );
        builder.addText(text, WIDTH - 22, 20)
                .setPosition(22, 0)
                .setColor(0xFF505050)
                .setLineSpacing(0)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setTextAlignment(HorizontalAlignment.CENTER);

        List<IRecipeSlotDrawable> outputSlots = builder.getRecipeSlots().getSlots(RecipeIngredientRole.OUTPUT);
        IScrollGridWidget grid = builder.addScrollGridWidget(outputSlots, COLUMNS, VISIBLE_ROWS);
        grid.setPosition(0, 0, WIDTH, HEIGHT - FOOTER_HEIGHT, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

        builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT).getFirst()
                .setPosition(grid.getScreenRectangle().position().x() + 1, 1);
    }

    private static String formatChance(float chance) {
        float percent = chance * 100f;
        if (percent >= 10f) return String.format("%.0f%%", percent);
        if (percent >= 1f) return String.format("%.1f%%", percent);
        return String.format("%.2f%%", Math.max(percent, 0.01f));
    }

    @Override
    public void draw(Entry entry, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        StructureLootRecipe recipe = entry.recipe();
        var font = Minecraft.getInstance().font;

        int rolls = recipe.rolls() >= 0 ? recipe.rolls() : recipe.lootTables().size();
        Component summary = Component.translatable("jei.structureloot.summary", rolls, recipe.duration() / 20, recipe.rfPerTick());
        guiGraphics.text(font, summary, (WIDTH - font.width(summary)) / 2, HEIGHT - FOOTER_HEIGHT + 3, 0xFF404040, false);
    }
}
