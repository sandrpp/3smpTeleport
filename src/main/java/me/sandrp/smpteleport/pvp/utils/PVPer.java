package me.sandrp.smpteleport.pvp.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE;

public class PVPer {
    private static final Map<UUID, PvPTask> activePvPs = new HashMap<>();
    private static final int pvpDelay = 6; // in seconds

    public static void startPvP(PlayerEntity player) {
        UUID playerId = player.getUuid();

        // Cancel existing PvP if any
        if (activePvPs.containsKey(playerId)) {
            activePvPs.get(playerId).cancel();
            activePvPs.remove(playerId);
        }

        // Start new PvP
        PvPTask task = new PvPTask(player);
        activePvPs.put(playerId, task);
        task.start();

        // Apply effects
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA,
                (pvpDelay + 3) * 20,
                0,
                false,
                false
        ));
    }

    public static void cancelPvPIfActive(PlayerEntity player) {
        UUID playerId = player.getUuid();
        if (activePvPs.containsKey(playerId)) {
            activePvPs.get(playerId).cancel();
            activePvPs.remove(playerId);
            player.removeStatusEffect(StatusEffects.NAUSEA);
            player.playSoundToPlayer(SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 0.6F, 0.6F);
        }
    }

    private static class PvPTask extends Thread {
        private final PlayerEntity player;
        private boolean cancelled = false;

        public PvPTask(PlayerEntity player) {
            this.player = player;
        }

        public void cancel() {
            this.cancelled = true;
            this.interrupt();
        }

        @Override
        public void run() {
            try {
                // Wait for PvP delay
                for (int i = 0; i < pvpDelay * 10 && !cancelled; i++) {
                    Thread.sleep(100);
                    // Check if player is still sneaking
                    if (!player.isSneaking()) {
                        cancel();
                        Objects.requireNonNull(player.getServer()).execute(() -> cancelPvPIfActive(player));
                        return;
                    }
                }
                if (!cancelled) {
                    // Execute chat message on main thread
                    Objects.requireNonNull(player.getServer()).execute(() -> {
                        if (!cancelled && player.isSneaking()) {
                            // Beispiel: Permission synchronisieren
                            LuckPerms luckPerms = LuckPermsProvider.get();
                            User user = luckPerms.getUserManager().getUser(player.getUuid());
                            if (user != null) {
                                if (user.getCachedData().getMetaData().getSuffixes().containsValue("")) {
                                    user.data().remove(luckPerms.getNodeBuilderRegistry().forSuffix().suffix("").priority(101).build());
                                    luckPerms.getUserManager().saveUser(user);
                                    player.playSoundToPlayer(SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.PLAYERS, 0.6F, 0.65F);
                                } else {
                                    user.data().add(luckPerms.getNodeBuilderRegistry().forSuffix().suffix("").priority(101).build());
                                    luckPerms.getUserManager().saveUser(user);
                                    player.playSoundToPlayer(SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.PLAYERS, 0.6F, 0.6F);
                                }
                            }
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 4*20, 0, false, false, false));
                            activePvPs.remove(player.getUuid());
                        } else {
                            cancelPvPIfActive(player);
                        }
                    });
                }
            } catch (InterruptedException e) {
                // Thread was interrupted (cancelled)
            }
        }
    }
}
