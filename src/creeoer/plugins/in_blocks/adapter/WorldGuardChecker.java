package creeoer.plugins.in_blocks.adapter;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * WorldGuard protection checker adapter.
 * Uses the modern WorldGuard 7.x API to check build permissions.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class WorldGuardChecker implements iChecker {

    /**
     * Creates a new WorldGuardChecker.
     */
    public WorldGuardChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            // Get the region container
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            if (container == null) {
                return true;
            }

            // Get the region manager for the world
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager == null) {
                return true;
            }

            // Convert location to BlockVector3
            BlockVector3 pos = BlockVector3.at(location.getX(), location.getY(), location.getZ());

            // Get applicable regions at the location
            ApplicableRegionSet regions = regionManager.getApplicableRegions(pos);

            // If no regions, allow building
            if (regions.size() == 0) {
                return true;
            }

            // Wrap the player
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            // Check if the player can build
            return regions.testState(localPlayer, Flags.BUILD);

        } catch (Exception e) {
            // If WorldGuard check fails, allow by default
            return true;
        }
    }
}
