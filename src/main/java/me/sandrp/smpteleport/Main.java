package me.sandrp.smpteleport;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.sandrp.smpteleport.commands.DeletePosCommand;
import me.sandrp.smpteleport.commands.ListPosCommand;
import me.sandrp.smpteleport.commands.SetPosCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Main implements ModInitializer {

    private static MiniMessage miniMessage;
    private static FileStorageManager fileStorageManager;

    @Override
    public void onInitialize() {

        miniMessage = MiniMessage.miniMessage();

        Path configDir = FabricLoader.getInstance().getGameDir().resolve("config");
        fileStorageManager = new FileStorageManager(configDir);

        //register commands

        //pos command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, executor) -> {
            dispatcher.register(CommandManager.literal("setpos").
                    then(CommandManager.argument("name", StringArgumentType.string()).
                            executes(new SetPosCommand())));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, executor) -> {
            dispatcher.register(CommandManager.literal("delpos")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                List<String> positionNames = new ArrayList<>();
                                fileStorageManager.getAllCoordinates().forEach(coordinate -> { positionNames.add(coordinate.getName()); });
                                return CommandSource.suggestMatching(positionNames, builder);
                            })
                            .executes(new DeletePosCommand())));
        });

        //listpos command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, executor) -> {
            dispatcher.register(CommandManager.literal("listpos").executes(new ListPosCommand()));
        });
    }

    public static FileStorageManager getFileStorageManager() {
        return fileStorageManager;
    }
    public static MiniMessage getMiniMessage() { return miniMessage; }

}
