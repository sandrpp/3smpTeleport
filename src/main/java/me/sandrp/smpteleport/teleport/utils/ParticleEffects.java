package me.sandrp.smpteleport.teleport.utils;

import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.concurrent.RunnableFuture;

public class ParticleEffects {

    public static void particleCircle(ServerWorld world, Vec3d pos, double radius, int particles, ParticleEffect particleType) {

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);

            Vec3d particlePos = new Vec3d(
                    pos.x + xOffset,
                    pos.y,
                    pos.z + zOffset
            );

            world.spawnParticles(
                    particleType,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
}