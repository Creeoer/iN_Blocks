package creeoer.plugins.in_blocks.main;

import creeoer.plugins.in_blocks.adapter.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages integration with protection plugins to validate schematic placement.
 * Uses the adapter pattern to support multiple protection plugins through a
 * common interface.
 *
 * <p>Supported protection plugins:
 * <ul>
 *   <li>WorldGuard</li>
 *   <li>GriefPrevention</li>
 *   <li>Towny</li>
 *   <li>Factions (both legacy and UUID versions)</li>
 *   <li>PreciousStones</li>
 *   <li>RedProtect</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class RegionManager {

    private final iN_Blocks plugin;
    private final List<iChecker> checkers;
    private final FileConfiguration config;

    /**
     * Creates a new RegionManager and registers all detected protection plugins.
     *
     * @param plugin The main plugin instance
     */
    protected RegionManager(iN_Blocks plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.checkers = new ArrayList<>();

        registerProtectionPlugins();
    }

    /**
     * Registers protection plugin adapters based on detected dependencies.
     */
    private void registerProtectionPlugins() {
        Set<String> dependencies = plugin.getDependencies();

        for (String pluginName : dependencies) {
            try {
                iChecker checker = createChecker(pluginName);
                if (checker != null) {
                    checkers.add(checker);
                    plugin.getLogger().info("Registered protection checker: " + pluginName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to register protection checker for: " + pluginName, e);
            }
        }
    }

    /**
     * Creates a checker instance for the specified protection plugin.
     *
     * @param pluginName The name of the protection plugin
     * @return The checker instance, or null if not supported
     */
    private iChecker createChecker(String pluginName) {
        switch (pluginName) {
            case "WorldGuard":
                return new WorldGuardChecker();
            case "PreciousStones":
                return new PreciousStonesChecker();
            case "GriefPrevention":
                return new GriefPreventionChecker();
            case "Towny":
                return new TownyChecker();
            case "RedProtect":
                return new RedProtectChecker();
            case "Factions":
                return isFactionsUUID() ?
                        new FactionsUUIDChecker() : new FactionsChecker();
            default:
                return null;
        }
    }

    /**
     * Checks if the Factions plugin is the UUID version.
     *
     * @return true if using Factions UUID
     */
    private boolean isFactionsUUID() {
        try {
            Class.forName("com.massivecraft.factions.entity.BoardColl");
            return false; // Old MassiveCraft Factions
        } catch (ClassNotFoundException e) {
            return true; // Factions UUID or not installed
        }
    }

    /**
     * Checks if a player can place a schematic at the specified location.
     * Validates placement against all registered protection plugins.
     *
     * @param player The player attempting to place
     * @param schematic The schematic being placed
     * @param placementLocation The location where placement starts
     * @return true if placement is allowed, false if blocked by any protection
     */
    public boolean canPlayerPlace(Player player, BuildSchematic schematic, Location placementLocation) {
        // Operators bypass all protection checks
        if (player.isOp()) {
            return true;
        }

        // No protection plugins = always allowed
        if (checkers.isEmpty()) {
            return true;
        }

        // Generate all locations that need to be checked
        List<Location> locationsToCheck = generateCheckLocations(
                placementLocation,
                schematic.getSizeX(),
                schematic.getSizeY(),
                schematic.getSizeZ()
        );

        // Check each location against all protection plugins
        for (Location location : locationsToCheck) {
            for (iChecker checker : checkers) {
                try {
                    if (!checker.isValidPlacement(location, player)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Log but don't fail - if a checker throws, skip it
                    plugin.getLogger().log(Level.WARNING,
                            "Protection checker error at " + location, e);
                }
            }
        }

        return true;
    }

    /**
     * Generates a list of locations to check for protection.
     * Includes a configurable radius around the schematic bounds.
     *
     * @param origin The origin location
     * @param sizeX The X dimension
     * @param sizeY The Y dimension
     * @param sizeZ The Z dimension
     * @return List of locations to check
     */
    private List<Location> generateCheckLocations(Location origin, int sizeX, int sizeY, int sizeZ) {
        List<Location> locations = new ArrayList<>();
        int checkRadius = config.getInt("Options.check-radius", 5);

        // Check all blocks within the schematic bounds plus radius
        for (int x = -checkRadius; x <= sizeX + checkRadius; x++) {
            for (int y = 0; y <= sizeY + checkRadius; y++) {
                for (int z = -checkRadius; z <= sizeZ + checkRadius; z++) {
                    locations.add(origin.clone().add(x, y, z));
                }
            }
        }

        return locations;
    }

    /**
     * Gets the list of registered protection checkers.
     *
     * @return List of iChecker instances
     */
    public List<iChecker> getCheckers() {
        return new ArrayList<>(checkers);
    }

    /**
     * Checks if any protection plugins are registered.
     *
     * @return true if at least one protection plugin is active
     */
    public boolean hasProtectionPlugins() {
        return !checkers.isEmpty();
    }
}
