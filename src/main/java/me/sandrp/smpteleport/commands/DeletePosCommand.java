package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.sandrp.smpteleport.Main;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DeletePosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if(!(source.getEntity() instanceof ServerPlayerEntity player)) return 1;

        String name = StringArgumentType.getString(context, "name");
        Main.getFileStorageManager().deleteCoordinate(name);
        player.sendMessage(Main.getMiniMessage().deserialize("<#56bc66>Position erfolgreich gelöscht</#56bc66>"));

        return 0;
    }
}