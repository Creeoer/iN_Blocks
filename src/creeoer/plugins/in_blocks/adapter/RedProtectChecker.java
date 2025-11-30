package creeoer.plugins.in_blocks.adapter;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * RedProtect protection checker adapter.
 * Uses the RedProtect API to check build permissions.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class RedProtectChecker implements iChecker {

    /**
     * Creates a new RedProtectChecker.
     */
    public RedProtectChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            // Get the region at this location
            Region region = RedProtect.get().getAPI().getRegion(location);

            // No region = can build
            if (region == null) {
                return true;
            }

            // Check if player can build in this region
            return region.canBuild(player);

        } catch (Exception e) {
            // If check fails, deny by default
            return false;
        }
    }
}
