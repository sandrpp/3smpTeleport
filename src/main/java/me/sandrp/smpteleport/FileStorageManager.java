package me.sandrp.smpteleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileStorageManager {
    private final File dataFile;
    private final Gson gson;
    private Map<String, Coordinate> coordinates;

    public FileStorageManager(Path configDir) {
        File modDataFolder = configDir.resolve("smpteleport").toFile();
        if (!modDataFolder.exists()) {
            modDataFolder.mkdirs();
        }

        this.dataFile = new File(modDataFolder, "coordinates.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.coordinates = new HashMap<>();
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Coordinate>>() {}.getType();
            Map<String, Coordinate> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                coordinates = loaded;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(coordinates, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCoordinate(String name, int x, int y, int z) {
        coordinates.put(name, new Coordinate(name, x, y, z));
        saveData();
    }

    public void deleteCoordinate(String name) {
        if (coordinates.remove(name) == null) {
            throw new RuntimeException("Coordinate with name '" + name + "' not found, nothing was deleted");
        }
        saveData();
    }

    public boolean isInCoordinates(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        ServerWorld world = (ServerWorld) player.getWorld();

        for (Coordinate coord : coordinates.values()) {
            if (pos.getX() == coord.getX() &&
                    pos.getY() == coord.getY() &&
                    pos.getZ() == coord.getZ() &&
                    world.getRegistryKey() == ServerWorld.OVERWORLD) {
                return true;
            }
        }
        return false;
    }

    public List<Coordinate> getAllCoordinates() {
        return new ArrayList<>(coordinates.values());
    }

    public static class Coordinate {
        private final String name;
        private final int x;
        private final int y;
        private final int z;

        public Coordinate(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getName() {
            return name;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }
}