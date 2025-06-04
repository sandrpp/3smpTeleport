package me.sandrp.smpteleport.mixin;

import me.sandrp.smpteleport.Main;
import me.sandrp.smpteleport.teleport.utils.Teleporter;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.sql.SQLException;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "setSneaking")
    private void setSneaking(boolean sneaking, CallbackInfo info) throws SQLException {
        Entity th = (Entity)(Object)this;
        if(th instanceof ServerPlayerEntity p)
        {
            boolean wasSneaking = p.isSneaking();
            if(!wasSneaking && sneaking && Main.getDatabaseManager().isInCoordinates(p.getBlockPos()))
            {
                Teleporter.teleportSpawn(p);
            }
        }
    }
}