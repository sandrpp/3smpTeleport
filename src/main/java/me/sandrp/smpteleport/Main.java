package me.sandrp.smpteleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.sandrp.smpteleport.commands.DeletePosCommand;
import me.sandrp.smpteleport.commands.ListPosCommand;
import me.sandrp.smpteleport.commands.SetPosCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Main implements ModInitializer {

    private static MiniMessage miniMessage;
    private static DatabaseManager databaseManager;

    @Override
    public void onInitialize() {

        miniMessage = MiniMessage.miniMessage();

        databaseManager = new DatabaseManager(
                "localhost",
                3306,
                "3smp",
                "sandrp",
                "test123"
        );

        try {
            databaseManager.connect();
            System.out.println("Connection with MySQL successful!");
        } catch (SQLException e) {
            System.err.println("Error connecting to MySQL!:");
            e.printStackTrace();
        }

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
                                // Get suggestions from database
                                try {
                                    List<String> positionNames = new ArrayList<>();
                                    databaseManager.getAllCoordinates().forEach(coordinate -> { positionNames.add(coordinate.getName()); });
                                    return CommandSource.suggestMatching(positionNames, builder);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    return builder.buildFuture();
                                }
                            })
                            .executes(new DeletePosCommand())));
        });

        //listpos command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, executor) -> {
            dispatcher.register(CommandManager.literal("listpos").executes(new ListPosCommand()));
        });
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public static MiniMessage getMiniMessage() { return miniMessage; }

}
