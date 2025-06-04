package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.sandrp.smpteleport.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.sql.SQLException;

public class SetPosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayer();

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
            source.sendMessage(Text.of("Position '" + name + "' wurde erfolgreich gespeichert"));
        } catch (SQLException e) {
            source.sendError(Text.of("Fehler beim Speichern der Koordinaten!"));
            e.printStackTrace();
        }

        return 1;
    }
}