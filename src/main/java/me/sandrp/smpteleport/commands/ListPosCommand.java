package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.sandrp.smpteleport.DatabaseManager;
import me.sandrp.smpteleport.Main;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.sql.SQLException;
import java.util.List;

public class ListPosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof PlayerEntity player)) { return 0; }

        try {
            List<DatabaseManager.Coordinate> posList = Main.getDatabaseManager().getAllCoordinates();
            String formatedPosList = "Positionen: " + String.join(", ",
                    posList.stream()
                            .map(coord -> String.format("%s (x:%d, y:%d, z:%d)",
                                    coord.getName(),
                                    coord.getX(),  // %d für Integer
                                    coord.getY(),
                                    coord.getZ()))
                            .toArray(String[]::new));

            source.sendMessage(Text.of(formatedPosList));
        } catch (SQLException e) {
            source.sendError(Text.of("Fehler beim Speichern der Koordinaten!"));
            e.printStackTrace();
        }

        return 1;
    }
}
