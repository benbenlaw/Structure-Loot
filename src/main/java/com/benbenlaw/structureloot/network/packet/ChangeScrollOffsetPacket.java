package com.benbenlaw.structureloot.network.packet;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.screen.custom.StructureLootMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record ChangeScrollOffsetPacket(int containerId, int scrollOffset) implements CustomPacketPayload {

    public static final Type<ChangeScrollOffsetPacket> TYPE = new Type<>(StructureLoot.identifier("change_scroll_offset"));


    public static final IPayloadHandler<ChangeScrollOffsetPacket> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.containerMenu instanceof StructureLootMenu menu && menu.containerId == packet.containerId()) {
                menu.setScrollOffset(packet.scrollOffset());
            }
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeScrollOffsetPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ChangeScrollOffsetPacket::containerId,
            ByteBufCodecs.INT, ChangeScrollOffsetPacket::scrollOffset,
            ChangeScrollOffsetPacket::new
    );

    @Override
    public Type<ChangeScrollOffsetPacket> type() {
        return TYPE;
    }


}