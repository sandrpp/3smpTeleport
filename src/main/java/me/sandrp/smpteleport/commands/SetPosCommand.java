package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.sandrp.smpteleport.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.sql.SQLException;

public class SetPosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if(!(source.getEntity() instanceof ServerPlayerEntity player)) { return 0; }

        BlockPos pos = player.getBlockPos();
        String world = player.getWorld().getRegistryKey().getValue().toString();

        String name = StringArgumentType.getString(context, "name");

        try {
            Main.getDatabaseManager().saveCoordinate(
                    name,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
            player.sendMessage(Main.getMiniMessage().deserialize("<#56bc66>Position erfolgreich erstellt</#56bc66>"));
        } catch (SQLException e) {
            player.sendMessage(Main.getMiniMessage().deserialize("<#e53051>Fehler beim erstellen der Position!</#e53051>"));
            e.printStackTrace();
        }

        return 1;
    }
}