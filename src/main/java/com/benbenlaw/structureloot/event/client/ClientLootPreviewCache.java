package com.benbenlaw.structureloot.event.client;

import com.benbenlaw.structureloot.network.packet.LootPreviewPacket;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientLootPreviewCache {

    private static Map<Identifier, List<LootPreviewPacket.Drop>> drops = new HashMap<>();
    private static Map<Identifier, List<LootPreviewPacket.Drop>> pending = new HashMap<>();
    private static Runnable listener = () -> {};

    public static void receive(LootPreviewPacket packet) {
        if (packet.index() == 0) pending = new HashMap<>();
        pending.put(packet.lootId(), packet.drops());

        if (packet.index() >= packet.total() - 1) {
            drops = pending;
            pending = new HashMap<>();
            listener.run();
        }
    }

    public static List<LootPreviewPacket.Drop> get(Identifier lootId) {
        return drops.getOrDefault(lootId, List.of());
    }

    public static void setListener(Runnable newListener) {
        listener = newListener;
    }
}
