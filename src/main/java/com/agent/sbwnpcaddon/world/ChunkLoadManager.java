package com.agent.sbwnpcaddon.world;

import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "sbw_npc_addon", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkLoadManager {

    // --- CONFIGURABLE CONSTANTS ---
    public static final int MAX_FORCELOAD_NPCS = 5;
    public static final int CHUNK_RADIUS = 1; // 1 means 3x3 (the chunk the NPC is in + 1 neighbor in all directions)
    private static final int SWEEP_INTERVAL_TICKS = 100; // Every 5 seconds at 20 TPS
    // ------------------------------

    // Tracks which NPCs currently have active force-loaded chunks, and what those chunks are.
    private static final Map<UUID, Set<ChunkPos>> activeNpcs = new ConcurrentHashMap<>();
    
    private static int tickCounter = 0;

    /**
     * Called every tick by the NPC's goal or tick event to maintain chunk loading around it.
     */
    public static void maintainChunkLoading(Mob mob) {
        if (mob.level().isClientSide || !(mob.level() instanceof ServerLevel serverLevel)) return;

        boolean hasActiveMove = false;
        if (mob.getPersistentData().getBoolean("SbwCommandActive")) {
            int mode = mob.getPersistentData().getInt("SbwCommandMode");
            if (mode == 2 || mode == 3) {
                hasActiveMove = true;
            }
        }

        UUID uuid = mob.getUUID();
        Set<ChunkPos> loadedChunks = activeNpcs.get(uuid);

        if (!hasActiveMove) {
            if (loadedChunks != null) {
                releaseAllForUUID(serverLevel, uuid);
            }
            return;
        }

        // If this NPC is not yet tracked, check if we have capacity
        if (loadedChunks == null) {
            if (activeNpcs.size() >= MAX_FORCELOAD_NPCS) {
                return; // Cap reached, do not force-load for this NPC
            }
            loadedChunks = new HashSet<>();
            activeNpcs.put(uuid, loadedChunks);
        }

        // Calculate desired chunks (3x3 grid around current chunk)
        ChunkPos currentChunk = new ChunkPos(mob.blockPosition());
        Set<ChunkPos> desiredChunks = new HashSet<>();
        for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
            for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                desiredChunks.add(new ChunkPos(currentChunk.x + x, currentChunk.z + z));
            }
        }

        // Release chunks that are no longer in the desired 3x3
        Iterator<ChunkPos> it = loadedChunks.iterator();
        while (it.hasNext()) {
            ChunkPos pos = it.next();
            if (!desiredChunks.contains(pos)) {
                ForgeChunkManager.forceChunk(serverLevel, "sbw_npc_addon", mob, pos.x, pos.z, false, false);
                it.remove();
            }
        }

        // Request new chunks that aren't already loaded
        for (ChunkPos pos : desiredChunks) {
            if (!loadedChunks.contains(pos)) {
                ForgeChunkManager.forceChunk(serverLevel, "sbw_npc_addon", mob, pos.x, pos.z, true, true);
                loadedChunks.add(pos);
            }
        }
    }

    /**
     * Releases all chunks for a specific NPC.
     */
    public static void releaseAllFor(Mob mob) {
        if (mob.level().isClientSide || !(mob.level() instanceof ServerLevel serverLevel)) return;
        releaseAllForUUID(serverLevel, mob.getUUID());
    }

    private static void releaseAllForUUID(ServerLevel serverLevel, UUID uuid) {
        Set<ChunkPos> loadedChunks = activeNpcs.remove(uuid);
        if (loadedChunks != null) {
            for (ChunkPos pos : loadedChunks) {
                // We don't have the mob instance, but we can use the UUID form of the API if available, 
                // but actually ForgeChunkManager remembers it by UUID anyway if we use the right method.
                // If we pass a dummy or just use vanilla, wait!
                // Actually in 1.20.1 ForgeChunkManager requires the UUID for entity tickets.
                ForgeChunkManager.forceChunk(serverLevel, "sbw_npc_addon", uuid, pos.x, pos.z, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter >= SWEEP_INTERVAL_TICKS) {
            tickCounter = 0;
            sweepOrphans(event.getServer());
        }
    }

    /**
     * Periodic sweep to release tickets for NPCs that no longer exist or are no longer active.
     */
    private static void sweepOrphans(net.minecraft.server.MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Iterator<Map.Entry<UUID, Set<ChunkPos>>> it = activeNpcs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Set<ChunkPos>> entry = it.next();
                UUID uuid = entry.getKey();
                
                net.minecraft.world.entity.Entity entity = level.getEntity(uuid);
                
                // If entity is completely gone, or it's dead, or it doesn't have an active SbwCommandGoal
                boolean shouldRelease = false;
                
                if (entity == null || !entity.isAlive() || !(entity instanceof Mob mob)) {
                    shouldRelease = true;
                } else {
                    // Check if it has an active command that warrants chunk loading (Mode 2 Move or Mode 3 Patrol)
                    boolean hasActiveMove = false;
                    if (mob.getPersistentData().getBoolean("SbwCommandActive")) {
                        int mode = mob.getPersistentData().getInt("SbwCommandMode");
                        if (mode == 2 || mode == 3) {
                            hasActiveMove = true;
                        }
                    }
                    if (!hasActiveMove) {
                        shouldRelease = true;
                    }
                }

                if (shouldRelease) {
                    for (ChunkPos pos : entry.getValue()) {
                        try {
                            ForgeChunkManager.forceChunk(level, "sbw_npc_addon", uuid, pos.x, pos.z, false, false);
                        } catch (Exception e) {}
                    }
                    it.remove();
                }
            }
        }
    }
}
