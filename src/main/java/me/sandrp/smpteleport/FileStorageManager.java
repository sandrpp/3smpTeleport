package me.sandrp.smpteleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileStorageManager {
    private final File dataFile;
    private final File pvpFile;
    private final Gson gson;
    private Map<String, Coordinate> coordinates;
    private Coordinate pvpCoordinate;

    public FileStorageManager(Path configDir) {
        File modDataFolder = configDir.resolve("smpteleport").toFile();
        if (!modDataFolder.exists()) {
            modDataFolder.mkdirs();
        }

        this.dataFile = new File(modDataFolder, "coordinates.json");
        this.pvpFile = new File(modDataFolder, "pvp_coordinate.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.coordinates = new HashMap<>();
        loadData();
        loadPvpCoordinate();
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

    private void saveCoordinates() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(coordinates, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- PVP Coordinate methods ---

    private void loadPvpCoordinate() {
        if (!pvpFile.exists()) {
            pvpCoordinate = null;
            return;
        }
        try (Reader reader = new FileReader(pvpFile)) {
            pvpCoordinate = gson.fromJson(reader, Coordinate.class);
        } catch (IOException e) {
            e.printStackTrace();
            pvpCoordinate = null;
        }
    }

    private void savePvpCoordinate() {
        try (Writer writer = new FileWriter(pvpFile)) {
            gson.toJson(pvpCoordinate, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPvpCoordinate(int x, int y, int z) {
        pvpCoordinate = new Coordinate("PVP", x, y, z);
        savePvpCoordinate();
    }

    public boolean isOnPVPCoordinates(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        ServerWorld world = (ServerWorld) player.getWorld();


        if (pos.getX() == pvpCoordinate.x() &&
                pos.getY() == pvpCoordinate.y() &&
                pos.getZ() == pvpCoordinate.z() &&
                world.getRegistryKey() == ServerWorld.OVERWORLD) {
            return true;
        }
        return false;
    }

    // --- End PVP Coordinate methods ---

    public void setCoordinate(String name, int x, int y, int z) {
        coordinates.put(name, new Coordinate(name, x, y, z));
        saveCoordinates();
    }

    public void deleteCoordinate(String name) {
        if (coordinates.remove(name) == null) {
            throw new RuntimeException("Coordinate with name '" + name + "' not found, nothing was deleted");
        }
        saveCoordinates();
    }

    public boolean isInCoordinates(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        ServerWorld world = (ServerWorld) player.getWorld();

        for (Coordinate coord : coordinates.values()) {
            if (pos.getX() == coord.x() &&
                    pos.getY() == coord.y() &&
                    pos.getZ() == coord.z() &&
                    world.getRegistryKey() == ServerWorld.OVERWORLD) {
                return true;
            }
        }
        return false;
    }

    public List<Coordinate> getAllCoordinates() {
        return new ArrayList<>(coordinates.values());
    }

    public record Coordinate(String name, int x, int y, int z) {
    }
}