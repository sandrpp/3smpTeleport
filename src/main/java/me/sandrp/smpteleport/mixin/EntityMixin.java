package me.sandrp.smpteleport.mixin;

import me.sandrp.smpteleport.Main;
import me.sandrp.smpteleport.pvp.utils.PVPer;
import me.sandrp.smpteleport.teleport.utils.Teleporter;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "setSneaking")
    private void setSneaking(boolean sneaking, CallbackInfo info) {
        Entity th = (Entity)(Object)this;
        if (th instanceof ServerPlayerEntity player) {
            boolean wasSneaking = player.isSneaking();

            if (!wasSneaking && sneaking) {
                if (Main.getFileStorageManager().isInCoordinates(player)) {
                    Teleporter.startTeleportSpawn(player);
                } else if (Main.getFileStorageManager().isOnPVPCoordinates(player)) {
                    PVPer.startPvP(player);
                }
            }
            else if (wasSneaking && !sneaking) {
                Teleporter.cancelTeleportIfActive(player);
            }
        }
    }
}