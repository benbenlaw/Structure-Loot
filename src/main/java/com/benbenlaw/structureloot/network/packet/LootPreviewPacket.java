package com.benbenlaw.structureloot.network.packet;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.event.client.ClientLootPreviewCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

public record LootPreviewPacket(int index, int total, Identifier lootId, List<Drop> drops) implements CustomPacketPayload {

    public static final Type<LootPreviewPacket> TYPE = new Type<>(StructureLoot.identifier("loot_preview"));

    public record Drop(ItemStack stack, List<ItemStack> variants, int min, int max, float chance, List<Component> notes) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Drop> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Drop::stack,
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), Drop::variants,
                ByteBufCodecs.VAR_INT, Drop::min,
                ByteBufCodecs.VAR_INT, Drop::max,
                ByteBufCodecs.FLOAT, Drop::chance,
                ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), Drop::notes,
                Drop::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, LootPreviewPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LootPreviewPacket::index,
            ByteBufCodecs.VAR_INT, LootPreviewPacket::total,
            Identifier.STREAM_CODEC, LootPreviewPacket::lootId,
            Drop.STREAM_CODEC.apply(ByteBufCodecs.list()), LootPreviewPacket::drops,
            LootPreviewPacket::new
    );

    public static final IPayloadHandler<LootPreviewPacket> HANDLER = (packet, context) ->
            context.enqueueWork(() -> ClientLootPreviewCache.receive(packet));

    @Override
    public Type<LootPreviewPacket> type() {
        return TYPE;
    }
}
