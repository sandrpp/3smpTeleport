package me.sandrp.smpteleport;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseManager {
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        connection = DriverManager.getConnection(url, username, password);

        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Coordinates (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL UNIQUE," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL)");
        stmt.close();
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void saveCoordinate(String name, int x, int y, int z) throws SQLException {
        String sql = "INSERT INTO Coordinates (name, x, y, z) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y), z = VALUES(z)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setInt(4, z);
            stmt.executeUpdate();
        }
    }

    public void deleteCoordinate(String name) throws SQLException {
        String sql = "DELETE FROM Coordinates WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Coordinate with name '" + name + "' not found, nothing was deleted");
            }
        }
    }

    public boolean isInCoordinates(PlayerEntity player) throws SQLException {
        BlockPos pos = player.getBlockPos();
        ServerWorld world = (ServerWorld) player.getWorld();

        String sql = "SELECT x, y, z FROM Coordinates";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if (pos.getX() == rs.getInt("x") &&
                        pos.getY() == rs.getInt("y") &&
                        pos.getZ() == rs.getInt("z") &&
                        world.getRegistryKey() == ServerWorld.OVERWORLD) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<Coordinate> getAllCoordinates() throws SQLException {
        List<Coordinate> coordinates = new ArrayList<>();
        String sql = "SELECT name, x, y, z FROM Coordinates";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                coordinates.add(new Coordinate(
                        rs.getString("name"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                ));
            }
        }
        return coordinates;
    }


    public class Coordinate {
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