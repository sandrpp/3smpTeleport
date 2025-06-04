package me.sandrp.smpteleport.teleport.utils;

import me.sandrp.smpteleport.DatabaseManager;
import me.sandrp.smpteleport.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.sql.SQLException;
import java.util.Collections;

public class Teleporter {

    public static void teleportSpawn(PlayerEntity player) {

        final double radius = 1;
        final int particles = 30;
        final ParticleEffect particleEffect = ParticleTypes.END_ROD;

        //effects
        player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);


        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d centerPos = player.getPos();
        int levels = 7;
        float yMod = 2;

        for (int i = 0; i < levels; i++) {
            yMod = yMod - 0.33f;

            if (player.isRemoved()) return;
            Vec3d currentPos = centerPos.add(0, yMod, 0);
            ParticleEffects.particleCircle(world, currentPos, radius, particles, ParticleTypes.END_ROD);

            try {
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //get spawn
        final BlockPos spawnPos = player.getWorld().getSpawnPos();

        //teleport player
        player.teleport((ServerWorld) player.getWorld(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), Collections.emptySet(), player.getYaw(), player.getPitch(), true);

    }

}
