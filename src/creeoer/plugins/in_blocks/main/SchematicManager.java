package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.session.ClipboardHolder;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages schematic files for the iN_Blocks plugin.
 * Handles creation, deletion, and retrieval of schematics using the
 * modern WorldEdit 7.x API with Sponge Schematic format.
 *
 * <p>Schematics are stored in the plugin's schematics directory with .schem extension.
 * Metadata about each schematic (facing direction) is stored in schematics.yml.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class SchematicManager {

    private final iN_Blocks plugin;
    private final File schematicsConfigFile;
    private final File schematicsDirectory;
    private FileConfiguration schematicsConfig;
    private final List<String> permissionRequiredSchematics;

    /**
     * Creates a new SchematicManager instance.
     *
     * @param plugin The main plugin instance
     */
    protected SchematicManager(iN_Blocks plugin) {
        this.plugin = plugin;
        this.schematicsConfigFile = new File(plugin.getDataFolder(), "schematics.yml");
        this.schematicsDirectory = new File(plugin.getDataFolder(), "schematics");
        this.permissionRequiredSchematics = new ArrayList<>();

        // Ensure directories exist
        if (!schematicsDirectory.exists()) {
            schematicsDirectory.mkdirs();
        }

        // Load schematic metadata
        this.schematicsConfig = YamlConfiguration.loadConfiguration(schematicsConfigFile);

        // Load permission-required schematics from config
        loadPermissionSchematics();
    }

    /**
     * Loads the list of schematics that require special permissions.
     */
    private void loadPermissionSchematics() {
        FileConfiguration config = plugin.getConfig();
        String permString = config.getString("Options.permissions", "");

        if (permString == null || permString.isEmpty()) {
            return;
        }

        // Clean up the string and parse
        String cleaned = permString.replace("[", "").replace("]", "");
        if (cleaned.contains(",")) {
            for (String schematic : cleaned.split(",")) {
                permissionRequiredSchematics.add(schematic.trim());
            }
        } else if (!cleaned.isEmpty()) {
            permissionRequiredSchematics.add(cleaned.trim());
        }
    }

    /**
     * Creates a new schematic from the player's WorldEdit clipboard.
     * Uses the modern Sponge Schematic v3 format for optimal compatibility.
     *
     * @param player The player creating the schematic
     * @param schematicName The name for the new schematic
     * @param direction The facing direction when the schematic was created
     * @return true if the schematic was created successfully
     */
    public boolean createSchematic(Player player, String schematicName, String direction) {
        // Get the WorldEdit actor for this player
        Actor actor = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        // Try to get the clipboard
        ClipboardHolder holder;
        try {
            holder = session.getClipboard();
        } catch (EmptyClipboardException e) {
            player.sendMessage(ChatColor.RED + Lang.EMPTY_CLIPBOARD.toString());
            return false;
        }

        Clipboard clipboard = holder.getClipboard();

        // Create output file with .schem extension (modern format)
        File outputFile = new File(schematicsDirectory, schematicName + ".schem");

        // Save the schematic using Sponge Schematic v3 format
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
                .getWriter(new FileOutputStream(outputFile))) {
            writer.write(clipboard);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save schematic: " + schematicName, e);
            player.sendMessage(ChatColor.RED + "Failed to save schematic!");
            return false;
        }

        // Save metadata
        schematicsConfig.set("Schematics." + schematicName, direction.toUpperCase());
        saveSchematicsConfig();

        return true;
    }

    /**
     * Deletes a schematic file and its metadata.
     *
     * @param schematicName The name of the schematic to delete
     * @return true if the schematic was deleted successfully
     */
    public boolean deleteSchematic(String schematicName) {
        File[] files = schematicsDirectory.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        boolean deleted = false;
        for (File file : files) {
            String fileName = file.getName();
            // Check both .schem and .schematic extensions
            if (fileName.equals(schematicName + ".schem") ||
                fileName.equals(schematicName + ".schematic")) {
                if (file.delete()) {
                    deleted = true;
                    break;
                }
            }
        }

        if (deleted) {
            // Remove metadata
            schematicsConfig.set("Schematics." + schematicName, null);
            saveSchematicsConfig();
        }

        return deleted;
    }

    /**
     * Checks if a schematic with the given name exists.
     *
     * @param schematicName The name to check
     * @return true if the schematic exists
     */
    public boolean doesExist(String schematicName) {
        File[] files = schematicsDirectory.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        for (File file : files) {
            String fileName = file.getName();
            // Check both .schem and .schematic extensions
            if (fileName.equals(schematicName + ".schem") ||
                fileName.equals(schematicName + ".schematic")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a BuildSchematic instance for the given schematic name.
     *
     * @param schematicName The name of the schematic
     * @return The BuildSchematic, or null if it doesn't exist or failed to load
     */
    public BuildSchematic getSchematic(String schematicName) {
        try {
            BuildSchematic schematic = new BuildSchematic(schematicName, plugin);
            if (schematic.isValid()) {
                return schematic;
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load schematic: " + schematicName, e);
            return null;
        }
    }

    /**
     * Checks if a schematic requires special permission to use.
     *
     * @param schematicName The name of the schematic
     * @return true if the schematic requires permission
     */
    public boolean hasPermission(String schematicName) {
        return permissionRequiredSchematics.contains(schematicName);
    }

    /**
     * Gets the schematics configuration file for reading metadata.
     *
     * @return The YamlConfiguration for schematics.yml
     */
    public YamlConfiguration getSchematicsFile() {
        return (YamlConfiguration) schematicsConfig;
    }

    /**
     * Gets a list of all available schematic names.
     *
     * @return List of schematic names (without extensions)
     */
    public List<String> getSchematicNames() {
        List<String> names = new ArrayList<>();
        File[] files = schematicsDirectory.listFiles();

        if (files == null) {
            return names;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".schem")) {
                names.add(fileName.substring(0, fileName.length() - 6));
            } else if (fileName.endsWith(".schematic")) {
                names.add(fileName.substring(0, fileName.length() - 10));
            }
        }

        return names;
    }

    /**
     * Reloads the schematics configuration from disk.
     */
    public void reload() {
        schematicsConfig = YamlConfiguration.loadConfiguration(schematicsConfigFile);
        permissionRequiredSchematics.clear();
        loadPermissionSchematics();
    }

    /**
     * Saves the schematics configuration to disk.
     */
    private void saveSchematicsConfig() {
        try {
            schematicsConfig.save(schematicsConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save schematics.yml", e);
        }
    }
}
