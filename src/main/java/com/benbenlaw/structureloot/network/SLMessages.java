package com.benbenlaw.structureloot.network;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.network.packet.ChangeScrollOffsetPacket;
import com.benbenlaw.structureloot.network.packet.LootPreviewPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SLMessages {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(StructureLoot.MOD_ID);

        //Client -> Server
        registrar.playToServer(ChangeScrollOffsetPacket.TYPE, ChangeScrollOffsetPacket.STREAM_CODEC, ChangeScrollOffsetPacket.HANDLER);

        //Server -> Client
        registrar.playToClient(LootPreviewPacket.TYPE, LootPreviewPacket.STREAM_CODEC, LootPreviewPacket.HANDLER);
    }
}
