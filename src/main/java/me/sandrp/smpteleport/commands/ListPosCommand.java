package me.sandrp.smpteleport.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import me.sandrp.smpteleport.FileStorageManager;
import me.sandrp.smpteleport.Main;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public class ListPosCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {

        ServerCommandSource source = context.getSource();
        if(!(source.getEntity() instanceof ServerPlayerEntity player)) return 0;

        List<FileStorageManager.Coordinate> posList = Main.getFileStorageManager().getAllCoordinates();
        String formatedPosList = "<#d2b4dc>Positionen:</#d2b4dc> " + String.join(", ",
                posList.stream()
                        .map(coord -> String.format("'%s' <gray><click:run_command:tp %d %d %d><hover:show_text:'<grey>/tp %d %d %d'>(%d, %d, %d)</gray>",
                                coord.name(),
                                coord.x(),
                                coord.y(),
                                coord.z(),
                                coord.x(),
                                coord.y(),
                                coord.z(),
                                coord.x(),
                                coord.y(),
                                coord.z()))
                        .toArray(String[]::new));

        player.sendMessage(Main.getMiniMessage().deserialize(formatedPosList));

        return 1;
    }
}
