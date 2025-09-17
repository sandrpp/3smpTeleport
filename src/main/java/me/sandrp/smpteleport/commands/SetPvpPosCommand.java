package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.sandrp.smpteleport.Main;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class SetPvpPosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if(!(source.getEntity() instanceof ServerPlayerEntity player)) return 1;

        BlockPos pos = player.getBlockPos();

        Main.getFileStorageManager().setPvpCoordinate(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
        player.sendMessage(Main.getMiniMessage().deserialize("<#56bc66>PVP Position erfolgreich erstellt</#56bc66>"));


        return 0;
    }
}