package com.benbenlaw.structureloot.screen.custom;

import com.benbenlaw.core.Core;
import com.benbenlaw.core.screen.util.DurationTooltip;
import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.network.packet.ChangeScrollOffsetPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class StructureLootScreen extends AbstractContainerScreen<StructureLootMenu> {

    private static final Identifier TEXTURE = StructureLoot.identifier("textures/gui/structure_loot_gui.png");
    private static final Identifier PROGRESS_ARROW = Core.identifier("progress_arrow");
    private static final Identifier SCROLL_ICON = StructureLoot.identifier("scroll");

    private boolean isDraggingScrollbar = false;

    public StructureLootScreen(StructureLootMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        if (menu.isCrafting()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 24, 16, 0, 0,x + 31, y + 35, menu.getScaledProgress() + 1, 16);
        }

        float max = menu.getMaxScroll();
        float scroll = max == 0 ? 0 : ((float) menu.getScrollOffset() / max);

        int barY = y + 17 + (int)(scroll * 37);

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_ICON, 12, 15, 0, 0, x + 154, barY, 12, 15);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        DurationTooltip.renderDurationTooltip(guiGraphics, mouseX, mouseY, x, y, 161, 5, menu.data.get(0), menu.data.get(1));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = menu.getMaxScroll();
        if (max <= 0) return false;

        int next = menu.getScrollOffset() - (int) Math.signum(scrollY);
        next = Math.max(0, Math.min(max, next));

        menu.setScrollOffset(next);

        ClientPacketDistributor.sendToServer(new ChangeScrollOffsetPacket(menu.containerId, next));

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = leftPos + 154;
        int y = topPos + 17;

        if (event.button() == 0 &&
                event.x() >= x && event.x() < x + 12 &&
                event.y() >= y && event.y() < y + 52) {
            isDraggingScrollbar = true;
            updateScrollFromMouse((int) event.y());
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) isDraggingScrollbar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (isDraggingScrollbar) {
            updateScrollFromMouse((int) event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    private void updateScrollFromMouse(int mouseY) {
        int top = topPos + 17 + 7;
        int bottom = topPos + 17 + 37 + 7;
        float ratio = (float)(mouseY - top) / (bottom - top);
        ratio = Math.max(0, Math.min(1, ratio));

        int max = menu.getMaxScroll();
        int next = Math.round(ratio * max);

        menu.setScrollOffset(next);
        ClientPacketDistributor.sendToServer(new ChangeScrollOffsetPacket(menu.containerId, next));
    }
}