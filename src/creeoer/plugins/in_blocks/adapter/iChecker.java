package creeoer.plugins.in_blocks.adapter;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Interface for protection plugin adapters.
 * Implementations check if a player can build at a specific location.
 *
 * <p>This interface follows the Adapter pattern to provide a unified
 * API for checking build permissions across different protection plugins.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public interface iChecker {

    /**
     * Checks if a player is allowed to place blocks at the specified location.
     *
     * @param location The location to check
     * @param player The player attempting to build
     * @return true if the player can build at this location, false otherwise
     */
    boolean isValidPlacement(Location location, Player player);
}
