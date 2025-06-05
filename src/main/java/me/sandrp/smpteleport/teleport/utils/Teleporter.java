package me.sandrp.smpteleport.teleport.utils;

import me.sandrp.smpteleport.Main;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Teleporter {

    private static final Map<UUID, TeleportTask> activeTeleports = new HashMap<>();
    private static final int teleportDelay = 6; // in seconds

    public static void startTeleportSpawn(PlayerEntity player) {
        UUID playerId = player.getUuid();

        // Cancel existing teleport if any
        if (activeTeleports.containsKey(playerId)) {
            activeTeleports.get(playerId).cancel();
            activeTeleports.remove(playerId);
        }

        // Start new teleport
        TeleportTask task = new TeleportTask(player);
        activeTeleports.put(playerId, task);
        task.start();

        // Apply effects
        player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA,
                (teleportDelay + 3) * 20,
                0,
                false,
                false
        ));
    }

    public static void cancelTeleportIfActive(PlayerEntity player) {
        UUID playerId = player.getUuid();
        if (activeTeleports.containsKey(playerId)) {
            activeTeleports.get(playerId).cancel();
            activeTeleports.remove(playerId);
        }
    }

    private static class TeleportTask extends Thread {
        private final PlayerEntity player;
        private boolean cancelled = false;

        public TeleportTask(PlayerEntity player) {
            this.player = player;
        }

        public void cancel() {
            this.cancelled = true;
            this.interrupt();
        }

        @Override
        public void run() {
            try {
                // Wait for teleport delay
                for (int i = 0; i < teleportDelay * 10 && !cancelled; i++) {
                    Thread.sleep(100);

                    // Check if player is still sneaking
                    if (!player.isSneaking()) {
                        cancel();
                        player.getServer().execute(() -> cancelTeleportIfActive(player));
                        return;
                    }
                }

                if (!cancelled) {
                    // Get spawn position
                    final BlockPos spawnPos = player.getWorld().getSpawnPos();

                    // Execute teleport on main thread
                    player.getServer().execute(() -> {
                        if (!cancelled && player.isSneaking()) {
                            player.teleport(
                                    (ServerWorld) player.getWorld(),
                                    spawnPos.getX(),
                                    spawnPos.getY(),
                                    spawnPos.getZ(),
                                    Collections.emptySet(),
                                    player.getYaw(),
                                    player.getPitch(),
                                    true
                            );
                            activeTeleports.remove(player.getUuid());
                        } else {
                            cancelTeleportIfActive(player);
                        }
                    });
                }
            } catch (InterruptedException e) {
                // Thread was interrupted (cancelled)
            }
        }
    }
}