package creeoer.plugins.in_blocks.objects;

import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages all active build tasks for the plugin.
 * Handles task creation, persistence, and lifecycle management.
 *
 * <p>Build tasks are persisted to currentBuilds.yml to allow resumption
 * after server restarts or player disconnections.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BuildManager {

    private final iN_Blocks plugin;
    private final Set<BuildTask> activeTasks;
    private final File buildsFile;
    private YamlConfiguration buildsConfig;

    /**
     * Creates a new BuildManager.
     *
     * @param plugin The main plugin instance
     */
    public BuildManager(iN_Blocks plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashSet<>();
        this.buildsFile = new File(plugin.getDataFolder(), "currentBuilds.yml");

        initializeBuildsFile();
        this.buildsConfig = YamlConfiguration.loadConfiguration(buildsFile);
    }

    /**
     * Initializes the builds file if it doesn't exist.
     */
    private void initializeBuildsFile() {
        if (!buildsFile.exists()) {
            try {
                buildsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create currentBuilds.yml", e);
            }
        }
    }

    /**
     * Creates and starts a new build task.
     *
     * @param schematic The schematic to build
     * @param chestBlock The chest block for material storage
     * @param playerName The owner's name
     */
    public void createNewTask(BuildSchematic schematic, Block chestBlock, String playerName) {
        BuildTask task = new BuildTask(schematic, chestBlock, playerName, plugin);
        task.setBuildTaskID(getNextTaskId());

        // Calculate tick interval based on blocks per second
        int blocksPerSecond = plugin.getConfig().getInt("Options.blocksPerSecond", 7);
        long tickInterval = Math.max(1, 20 / blocksPerSecond);

        task.runTaskTimer(plugin, 40, tickInterval);
        activeTasks.add(task);
        saveTask(task);
    }

    /**
     * Gets the next available task ID.
     *
     * @return The next unique task ID
     */
    private int getNextTaskId() {
        Set<String> keys = buildsConfig.getKeys(false);
        if (keys == null || keys.isEmpty()) {
            return 1;
        }

        List<Integer> ids = new ArrayList<>();
        for (String key : keys) {
            try {
                ids.add(Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
            }
        }

        return ids.isEmpty() ? 1 : Collections.max(ids) + 1;
    }

    /**
     * Checks if a player has an active or saved build task.
     *
     * @param playerName The player name to check
     * @return true if the player has a task
     */
    public boolean hasTask(String playerName) {
        // Check active tasks
        for (BuildTask task : activeTasks) {
            if (task.getOwnerName().equals(playerName)) {
                return true;
            }
        }

        // Check saved tasks
        Set<String> keys = buildsConfig.getKeys(false);
        if (keys != null) {
            for (String id : keys) {
                String savedPlayer = buildsConfig.getString(id + ".player");
                if (playerName.equals(savedPlayer)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Initializes saved tasks on plugin startup.
     * Removes expired tasks based on the configured expiry time.
     *
     * @throws IOException If there's an error saving the configuration
     */
    public void initTasks() throws IOException {
        Set<String> keys = buildsConfig.getKeys(false);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int expiryMinutes = plugin.getConfig().getInt("Options.expiry-time", 1200);
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        boolean modified = false;

        for (String id : new HashSet<>(keys)) {
            long startTime = buildsConfig.getLong(id + ".startTime", 0);
            long expiryTimeSeconds = (startTime / 1000) + (expiryMinutes * 60L);

            if (currentTimeSeconds >= expiryTimeSeconds) {
                buildsConfig.set(id, null);
                modified = true;
                plugin.getLogger().info("Removed expired task: " + id);
            }
        }

        if (modified) {
            buildsConfig.save(buildsFile);
        }
    }

    /**
     * Starts (resumes) a saved task for a player.
     *
     * @param playerName The player name
     * @return The started BuildTask, or null if not found
     * @throws IOException If there's an error with task creation
     */
    public BuildTask startTask(String playerName) throws IOException {
        Set<String> keys = buildsConfig.getKeys(false);
        if (keys == null) {
            return null;
        }

        for (String id : keys) {
            String savedPlayer = buildsConfig.getString(id + ".player");
            if (!playerName.equals(savedPlayer)) {
                continue;
            }

            String schematicName = buildsConfig.getString(id + ".name");
            Location location = (Location) buildsConfig.get(id + ".location");
            int place = buildsConfig.getInt(id + ".place", 0);

            if (schematicName == null || location == null) {
                continue;
            }

            BuildSchematic schematic = new BuildSchematic(schematicName, plugin);
            if (!schematic.isValid()) {
                continue;
            }

            Block chestBlock = getChestFromLocation(location);
            BuildTask task = new BuildTask(schematic, chestBlock, playerName, plugin);
            task.setPlace(place);
            task.setBuildTaskID(Integer.parseInt(id));

            int blocksPerSecond = plugin.getConfig().getInt("Options.blocksPerSecond", 7);
            long tickInterval = Math.max(1, 20 / blocksPerSecond);

            task.runTaskTimer(plugin, 0, tickInterval);
            activeTasks.add(task);

            return task;
        }

        return null;
    }

    /**
     * Removes a task from active tracking and persistence.
     *
     * @param task The task to remove
     */
    public void removeTask(BuildTask task) {
        activeTasks.remove(task);

        try {
            task.cancel();
        } catch (IllegalStateException ignored) {
            // Task may already be cancelled
        }

        String taskId = String.valueOf(task.getBuildTaskID());
        if (buildsConfig.contains(taskId)) {
            buildsConfig.set(taskId, null);
            saveBuildsConfig();
        }
    }

    /**
     * Gets the chest block from a saved location.
     *
     * @param location The saved placement location
     * @return The chest block
     */
    private Block getChestFromLocation(Location location) {
        return location.clone().add(-1, 0, -1).getBlock();
    }

    /**
     * Gets all active build tasks.
     *
     * @return Unmodifiable set of active tasks
     */
    public Set<BuildTask> getAllTasks() {
        return Collections.unmodifiableSet(activeTasks);
    }

    /**
     * Saves all active tasks to disk.
     */
    public void saveAllTasks() {
        for (BuildTask task : activeTasks) {
            saveTask(task);
        }
    }

    /**
     * Saves a single task to disk.
     *
     * @param task The task to save
     */
    public void saveTask(BuildTask task) {
        String taskId = String.valueOf(task.getBuildTaskID());

        buildsConfig.set(taskId + ".name", task.getSchematic().getName());
        buildsConfig.set(taskId + ".location", task.getLocation());
        buildsConfig.set(taskId + ".player", task.getOwnerName());
        buildsConfig.set(taskId + ".place", task.getPlace());
        buildsConfig.set(taskId + ".startTime", System.currentTimeMillis());

        saveBuildsConfig();
    }

    /**
     * Saves the builds configuration to disk.
     */
    private void saveBuildsConfig() {
        try {
            buildsConfig.save(buildsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save currentBuilds.yml", e);
        }
    }

    /**
     * Reloads the builds configuration from disk.
     */
    public void reload() {
        buildsConfig = YamlConfiguration.loadConfiguration(buildsFile);
    }
}
